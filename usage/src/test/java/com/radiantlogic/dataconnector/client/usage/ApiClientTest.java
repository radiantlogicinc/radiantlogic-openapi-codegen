package com.radiantlogic.dataconnector.client.usage;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

@WireMockTest(httpPort = 9000)
public @interface ApiClientTest {}
