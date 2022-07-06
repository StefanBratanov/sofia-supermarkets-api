addEventListener("fetch", (event) => {
	event.respondWith(
		handleRequest(event.request).catch(
			(err) => new Response(err.stack, { status: 500 })
		)
	);
});

/**
 * @param {Request} request
 * @returns {Promise<Response>}
 */
async function handleRequest(request) {
	const { search, pathname } = new URL(request.url)
	const queryPath = pathname + search
	const defaultHeaders = {
		'Access-Control-Allow-Origin': '*'
	}

	const cachedResponse = await CACHED_API_RESPONSES.get(queryPath)
	if (cachedResponse != null) {
		console.log("Using cached response from KV")
		return createJsonResponse(cachedResponse, 200, defaultHeaders)
	}

	const apiResponse = await fetch(`${API_HOST}${queryPath}`)
	const apiResponseBody = await apiResponse.text()

	if (apiResponse.status == 200) {
		// cache api responses for 30 minutes
		console.log("Saving response to KV")
		await CACHED_API_RESPONSES.put(queryPath, apiResponseBody, { expirationTtl: 1800 })
		return createJsonResponse(apiResponseBody, 200, defaultHeaders)
	}
	return new Response(apiResponseBody, { status: apiResponse.status, headers: defaultHeaders })
}

/**
 * @param {string} json
 * @param {number} status
 * @param {object} defaultHeaders
 * @returns {Response}
 */
function createJsonResponse(json, status, defaultHeaders) {
	return new Response(json, {
		status: status,
		headers: {
			'content-type': 'application/json;charset=UTF-8',
			...defaultHeaders
		},
	})
}
