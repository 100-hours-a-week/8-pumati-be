import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
    vus: 50,
    iterations: 200,
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000'],
    },
};

const BASE_URL = 'https://dev.tebutebu.com/api';
const MAX_RETRIES = 5;
const RETRY_DELAY_MS = 500;

function retrySnapshotCreation() {
    for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
        const res = http.post(`${BASE_URL}/projects/snapshot`, null, { timeout: '3s' });

        const passed = check(res, {
            'snapshot status is 201': (r) => r.status === 201,
            'snapshot body exists': (r) => !!r.body,
            'snapshot contains data.id': (r) => {
                try {
                    const json = r.json();
                    return json?.data?.id !== undefined;
                } catch (_) {
                    return false;
                }
            },
        });

        if (passed) return res;

        if (attempt < MAX_RETRIES) {
            console.warn(`Retrying snapshot creation (attempt ${attempt})...`);
            sleep(RETRY_DELAY_MS / 1000);
        } else {
            console.error(`Snapshot creation failed after ${MAX_RETRIES} attempts.`);
        }
    }

    return null;
}

export default function () {
    const snapshotRes = retrySnapshotCreation();
    if (!snapshotRes) return;

    let snapshotId;
    try {
        snapshotId = snapshotRes.json('data.id');
    } catch (e) {
        console.error('Failed to parse snapshot response:', e.message);
        return;
    }

    if (!snapshotId) {
        console.error('snapshotId not found in response');
        return;
    }

    const pageSize = randomIntBetween(5, 10);
    const rankRes = http.get(`${BASE_URL}/projects?sort=rank&context-id=${snapshotId}&cursor-id=0&page-size=${pageSize}`, { timeout: '3s' });

    check(rankRes, {
        'ranking fetch status 200': (r) => r.status === 200,
        'ranking response has body': (r) => !!r.body,
        'ranking response contains meta': (r) => {
            try {
                const json = r.json();
                return json?.meta !== undefined;
            } catch (e) {
                console.error('Failed to parse ranking response:', e.message);
                return false;
            }
        },
    });

    sleep(randomIntBetween(1, 3));
}
