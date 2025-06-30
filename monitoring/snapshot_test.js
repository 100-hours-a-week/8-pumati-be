import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 100, // 동시에 50명의 유저
    duration: '5s', // 5초 동안 반복
};

export default function () {
    const url = 'http://localhost:8080/api/projects/snapshot'; // 대상 API
    // const url = 'https://dev.tebutebu.com/api/projects/snapshot'; // 실제 API URL
    const res = http.post(url, null, {
        headers: {
            'Content-Type': 'application/json',
        },
    });

    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    sleep(0.1); // 약간의 딜레이를 줘서 과도한 폭주 방지
}
