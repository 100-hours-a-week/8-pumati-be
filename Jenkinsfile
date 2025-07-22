pipeline {
  agent any

  environment {
    PROJECT_NAME     = "pumati"                      // 프로젝트명
    SERVICE_NAME     = "backend"                     // 서비스명
    S3_BUCKET        = "s3-pumati-common-storage"    // S3 버킷
    AWS_REGION       = "ap-northeast-2"              // 리전
    AWS_ACCOUNT_ID   = "236450698266"                // 계정 ID
    HOST_PORT        = "8080"                        // 호스트 포트
    CONTAINER_PORT   = "8080"                        // 컨테이너 포트
    CONTAINER_NAME   = "pumati-backend"              // 컨테이너 이름
    ENV_PATH         = "/tmp/.env"                   // .env 파일 경로
  }

  stages {
    stage('Set Environment') {
      steps {
        echo """
        ============================================
        스테이지 시작: Set Environment
        ============================================
        """
        script {
          // 브랜치명 추출 및 환경 설정
          env.BRANCH = (env.BRANCH_NAME ?: env.GIT_BRANCH)?.replaceFirst(/^origin\//, '') ?: 'unknown'

          if (env.BRANCH == 'main') {
            env.ENV_LABEL = 'prod'
            env.BE_PRIVATE_IP = '10.3.0.107'
          } else {
            echo "지원되지 않는 브랜치입니다: ${env.BRANCH}. 빌드를 중단합니다."
            currentBuild.result = 'NOT_BUILT'
            error("Unsupported branch: ${env.BRANCH}")
          }

          // 타임스탬프 + 커밋 해시 생성
          def timestamp = new Date().format("yyyyMMdd-HHmmss", TimeZone.getTimeZone('Asia/Seoul'))
          def shortHash = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()

          env.ECR_REPO  = "${env.PROJECT_NAME}-${env.ENV_LABEL}-${env.SERVICE_NAME}-ecr"
          env.IMAGE_TAG = "${env.SERVICE_NAME}-${env.ENV_LABEL}-${env.BUILD_NUMBER}-${timestamp}-${shortHash}"
          env.ECR_IMAGE = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com/${env.ECR_REPO}:${env.IMAGE_TAG}"

          // 설정 확인 로그
          echo "현재 브랜치: ${env.BRANCH}"
          echo "환경 설정 완료"
          echo "IMAGE_TAG: ${env.IMAGE_TAG}"
          echo "ECR_IMAGE: ${env.ECR_IMAGE}"
        }
      }
    }

    stage('Notify Before Start') {
      when {
          expression { env.BRANCH == 'main' } // dev 브랜치 조건 제거
      }
      steps {
        echo """
        ============================================
        스테이지 시작: Notify Before Start
        ============================================
        """
        script {
          try {
            // Jenkins의 Credentials에서 'Discord-Webhook' ID를 사용하여 웹훅 URL을 가져옴
            withCredentials([string(credentialsId: 'Discord-Webhook', variable: 'DISCORD')]) {
              discordSend(
                description: "빌드가 곧 시작됩니다: ${env.SERVICE_NAME} - ${env.BRANCH} 브랜치",
                link: env.BUILD_URL,
                title: "빌드 시작",
                webhookURL: DISCORD
              )
            }
          } catch (e) {
            echo "디스코드 알림 전송 실패: ${e.message}"
          }
        }
      }
    }

    stage('Checkout') {
      steps {
        echo """
        ============================================
        스테이지 시작: Checkout
        ============================================
        """
        checkout scm
      }
    }

    stage('Fetch .env from AWS Secrets Manager') {
      steps {
        echo """
        ============================================
        스테이지 시작: Fetch .env from AWS Secrets Manager
        ============================================
        """
        script {
          try {
            // 1. Secrets Manager에서 .env 내용 가져오기
            def secret = sh(
              script: """
                set -e
                aws secretsmanager get-secret-value \
                  --secret-id ${env.PROJECT_NAME}-${env.ENV_LABEL}-${env.SERVICE_NAME}-.env \
                  --region ${env.AWS_REGION} \
                  --query SecretString \
                  --output text
              """,
              returnStdout: true
            ).trim()

            // 2. .env 파일로 저장
            writeFile file: '.env', text: secret

            // 3. 보안 강화를 위한 퍼미션 제한
            sh 'chmod 600 .env'

            echo ".env 파일 로딩 완료"
          } catch (e) {
            echo ".env 시크릿 로딩 실패: ${e.message}"
            currentBuild.result = 'FAILURE'
            error("빌드 중단: Secrets Manager에서 .env를 불러올 수 없습니다.")
          }
        }
      }
    }

    stage('Build JAR') {
      steps {
        sh './gradlew clean build -x test'
      }
    }
    
    stage('Rename JAR') {
      steps {
        sh 'mv build/libs/*.jar build/libs/backend.jar'
      }
    }

    stage('Authorize Docker to ECR') {
      steps {
        echo """
        ============================================
        스테이지 시작: Authorize Docker to ECR
        ============================================
        """
        script {
          sh """
            set -e
            aws ecr get-login-password --region ${env.AWS_REGION} | \
            docker login --username AWS --password-stdin ${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com
          """
          echo "ECR 인증 완료"
        }
      }
    }

    stage('Docker Build & Push to ECR') {
      steps {
        echo """
        ============================================
        스테이지 시작: Docker Build & Push to ECR
        ============================================
        """
        script {
          def dockerScript = """
            set -eux

            # Docker 이미지 빌드
            docker build -t ${env.ECR_IMAGE} .

            # latest 태그 추가
            LATEST_TAG="\$(echo ${env.ECR_IMAGE} | cut -d: -f1):latest"
            docker tag ${env.ECR_IMAGE} \$LATEST_TAG

            # ECR에 push
            docker push ${env.ECR_IMAGE}
            docker push \$LATEST_TAG
          """

          sh(script: dockerScript, shell: '/bin/bash')
          echo "Docker 이미지 빌드 및 ECR push 완료 (${env.IMAGE_TAG} + latest)"
        }
      }
    }

    stage('Save Docker Image & Upload to S3') {
      steps {
        echo """
        ============================================
        스테이지 시작: Save Docker Image & Upload to S3
        ============================================
        """
        script {
          def tarFile = "${env.IMAGE_TAG}.tar"
          def gzipFile = "${tarFile}.gz"

          sh """
            echo "Docker 이미지 저장: ${tarFile}"
            docker save -o ${tarFile} ${env.ECR_IMAGE}

            echo "압축 중: ${gzipFile}"
            gzip -c ${tarFile} > ${gzipFile}

            echo "S3에 업로드 중..."
            aws s3 cp ${gzipFile} s3://${env.S3_BUCKET}/CICD/${env.ENV_LABEL}/${env.SERVICE_NAME}/${gzipFile} --region ${env.AWS_REGION}

            echo "로컬 파일 정리"
            rm -f ${tarFile} ${gzipFile}

            echo "S3 업로드 완료"
          """
        }
      }
    }

    stage('Deploy to Backend EC2 via SSH') {
      steps {
        echo """
        ============================================
        스테이지 시작: Deploy to Backend EC2 via SSH
        ============================================
        """
        script {
          echo "EC2에 SSH 접속하여 백엔드 자동 배포 시작..."

          def ECR_LATEST_IMAGE = "${env.ECR_IMAGE.split(':')[0]}:latest"

          withCredentials([
            sshUserPrivateKey(credentialsId: 'PUMATI_FULL_MASTER', keyFileVariable: 'KEY_FILE', usernameVariable: 'SSH_USER')
          ]) {
            // 1. .env 파일 EC2로 전송 (권한 문제 없는 /tmp 사용)
            sh """
            echo "[단계1] .env 파일 전송 시작"
            scp -i \$KEY_FILE -o StrictHostKeyChecking=no .env \$SSH_USER@${env.BE_PRIVATE_IP}:${env.ENV_PATH}
            echo "[단계1] .env 파일 전송 완료"
            """

            // 2. 원격 서버에서 Docker 배포
            sh """
echo "[단계2] SSH 접속하여 Docker 배포 실행"
ssh -o StrictHostKeyChecking=no -i \$KEY_FILE \$SSH_USER@${env.BE_PRIVATE_IP} << 'EOF'
  set -e

  echo "[단계2-1] 기존 컨테이너 중지 및 제거"
  sudo docker stop ${env.CONTAINER_NAME} || true
  sudo docker rm ${env.CONTAINER_NAME} || true

  echo "[단계2-2] ECR 인증"
  aws ecr get-login-password --region ${env.AWS_REGION} | \\
    sudo docker login --username AWS --password-stdin ${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com

  echo "[단계2-3] ECR 이미지 Pull"
  sudo docker pull ${ECR_LATEST_IMAGE}

  echo "[단계2-4] 새 컨테이너 실행"
  sudo docker run -d \\
    --env-file ${env.ENV_PATH} \\
    -p ${env.HOST_PORT}:${env.CONTAINER_PORT} \\
    --name ${env.CONTAINER_NAME} \\
    ${ECR_LATEST_IMAGE}

  echo "[단계2-5] .env 파일 삭제"
  rm -f ${env.ENV_PATH}

  echo "[단계2-6] 사용하지 않는 이미지 정리"
  sudo docker image prune -a -f

  echo "[단계2-7] 배포 헬스 체크"
  sleep 30
  docker ps
  curl -i http://localhost:${env.HOST_PORT}/actuator/health

  echo "배포 완료"
EOF
            """
          }
        }
      }
    }
  }

  post {
    success {
      script {
        if (env.BRANCH == 'main') {
          withCredentials([string(credentialsId: 'Discord-Webhook', variable: 'DISCORD')]) {
            discordSend(
              description: """
              제목 : ${currentBuild.displayName}
              결과 : 성공
              배포 이미지 태그 : ${env.IMAGE_TAG}
              실행 시간 : ${currentBuild.duration / 1000}s
              """.stripIndent(),
              link: env.BUILD_URL,
              title: "${env.JOB_NAME} :: ${env.BRANCH} :: 배포 성공",
              result: 'SUCCESS',
              webhookURL: DISCORD
            )
          }
        }
      }
    }

    failure {
      script {
        if (env.BRANCH == 'main') {
          withCredentials([string(credentialsId: 'Discord-Webhook', variable: 'DISCORD')]) {
            discordSend(
              description: """
              제목 : ${currentBuild.displayName}
              결과 : 실패
              배포 이미지 태그 : ${env.IMAGE_TAG}
              실행 시간 : ${currentBuild.duration / 1000}s
              """.stripIndent(),
              link: env.BUILD_URL,
              title: "${env.JOB_NAME} :: ${env.BRANCH} :: 배포 실패",
              result: 'FAILURE',
              webhookURL: DISCORD
            )
          }
        }
      }
    }
  }
}
