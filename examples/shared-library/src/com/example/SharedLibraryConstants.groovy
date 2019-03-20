package com.example
@Grab(group='org.apache.httpcomponents', module='httpclient', version='4.5.7')
import org.apache.http.ssl.SSLContexts

public class SharedLibraryConstants {
	public static final String DEPLOY_COMMAND = """
	docker-compose pull && \
	docker-compose down && \
	docker-compose rm -f && \
	docker-compose up -d --force-recreate"""
}
