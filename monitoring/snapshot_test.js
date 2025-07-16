import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
    vus: 50,
    iterations: 200,
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000', 'p(99)<1500'],
    },
};

const BASE_URL = 'http://localhost:8080/api';
const MAX_RETRIES = 5;
const RETRY_DELAY_SEC = 0.5;

function retrySnapshotCreation() {
    for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
        const res = http.post(`${BASE_URL}/projects/snapshot`, null, { timeout: '3s' });

        try {
            const id = res.json('data.id');
            if (res.status === 201 && id !== undefined) {
                return id;
            }
        } catch (_) {
            // ignore parse error and retry
        }

        if (attempt < MAX_RETRIES) {
            console.warn(`Retrying snapshot creation (attempt ${attempt})...`);
            sleep(RETRY_DELAY_SEC);
        } else {
            console.error('Snapshot creation failed after max retries.');
        }
    }

    return null;
}

export default function () {
    const snapshotId = retrySnapshotCreation();
    if (!snapshotId) return;

    const pageSize = randomIntBetween(5, 10);
    const rankRes = http.get(`${BASE_URL}/projects?sort=rank&context-id=${snapshotId}&cursor-id=0&page-size=${pageSize}`, { timeout: '3s' });

    check(rankRes, {
        'ranking fetch status 200': (r) => r.status === 200,
        'ranking response has body': (r) => !!r.body,
        'ranking response contains meta': (r) => {
            try {
                const json = r.json();
                return json?.meta !== undefined;
            } catch (_) {
                return false;
            }
        },
    });

    sleep(randomIntBetween(1, 3));
}
