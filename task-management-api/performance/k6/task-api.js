import http from 'k6/http';
import { check, fail } from 'k6';

export const options = {
    vus: Number(__ENV.VUS || 1),
    duration: __ENV.DURATION || '1m'
};

const baseUrl = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const email = __ENV.PERF_EMAIL;
const password = __ENV.PERF_PASSWORD;

if (!email || !password) {
    throw new Error('Set PERF_EMAIL and PERF_PASSWORD to a non-production test account.');
}

let accessToken;
let refreshToken;

function expectStatus(response, expectedStatus, label) {
    const passed = check(response, {
        [label]: (result) => result.status === expectedStatus
    });
    if (!passed) {
        fail(`${label} did not return HTTP ${expectedStatus}.`);
    }
}

function jsonOptions(endpoint) {
    return {
        headers: { 'Content-Type': 'application/json' },
        tags: { endpoint }
    };
}

function bearerOptions(endpoint) {
    return {
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json'
        },
        tags: { endpoint }
    };
}

function authenticate() {
    const response = http.post(
        `${baseUrl}/api/v1/auth/login`,
        JSON.stringify({ email, password }),
        jsonOptions('login')
    );
    expectStatus(response, 200, 'login');
    const tokens = response.json();
    accessToken = tokens.accessToken;
    refreshToken = tokens.refreshToken;
}

function rotateRefreshToken() {
    const response = http.post(
        `${baseUrl}/api/v1/auth/refresh`,
        JSON.stringify({ refreshToken }),
        jsonOptions('refresh')
    );
    expectStatus(response, 200, 'refresh');
    const tokens = response.json();
    accessToken = tokens.accessToken;
    refreshToken = tokens.refreshToken;
}

export default function () {
    if (!refreshToken) {
        authenticate();
    }
    rotateRefreshToken();

    const title = `perf-vu-${__VU}-iteration-${__ITER}`;
    let taskId;
    try {
        const createResponse = http.post(
            `${baseUrl}/api/v1/tasks`,
            JSON.stringify({ title, description: 'Temporary performance-test task' }),
            bearerOptions('task_create')
        );
        expectStatus(createResponse, 201, 'task_create');
        taskId = createResponse.json().id;
        if (taskId === undefined || taskId === null) {
            fail('Task creation response did not contain an ID.');
        }

        const listResponse = http.get(
            `${baseUrl}/api/v1/tasks?page=0&size=100&sortBy=createdAt&direction=desc`,
            bearerOptions('task_list')
        );
        expectStatus(listResponse, 200, 'task_list');

        const taskUrl = `${baseUrl}/api/v1/tasks/${taskId}`;
        const getResponse = http.get(taskUrl, bearerOptions('task_get'));
        expectStatus(getResponse, 200, 'task_get');

        const updateResponse = http.put(
            taskUrl,
            JSON.stringify({
                title: `${title}-updated`,
                description: 'Temporary performance-test task',
                status: 'IN_PROGRESS',
                priority: 'MEDIUM',
                dueDate: null
            }),
            bearerOptions('task_update')
        );
        expectStatus(updateResponse, 200, 'task_update');
    } finally {
        if (taskId !== undefined && taskId !== null) {
            const deleteResponse = http.del(
                `${baseUrl}/api/v1/tasks/${taskId}`,
                null,
                bearerOptions('task_delete')
            );
            expectStatus(deleteResponse, 204, 'task_delete');
        }
    }
}