package nl.altindag.client.service;

import static nl.altindag.client.TestConstants.GET_METHOD;
import static nl.altindag.client.TestConstants.HEADER_KEY_CLIENT_TYPE;
import static nl.altindag.client.TestConstants.HTTP_URL;
import static nl.altindag.client.TestConstants.OK_HTTP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.altindag.client.model.ClientResponse;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@RunWith(MockitoJUnitRunner.class)
public class OkHttpClientWrapperShould {

    @InjectMocks
    private OkHttpClientWrapper victim;
    @Mock
    private OkHttpClient okHttpClient;

    @Test
    public void executeRequest() throws Exception {
        Call call = mock(Call.class);
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);

        when(okHttpClient.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("Hello");
        when(response.code()).thenReturn(200);

        ArgumentCaptor<Request> requestArgumentCaptor = ArgumentCaptor.forClass(Request.class);

        ClientResponse clientResponse = victim.executeRequest(HTTP_URL);

        assertThat(clientResponse.getStatusCode()).isEqualTo(200);
        assertThat(clientResponse.getResponseBody()).isEqualTo("Hello");

        verify(okHttpClient, times(1)).newCall(requestArgumentCaptor.capture());
        assertThat(requestArgumentCaptor.getValue().url().toString()).isEqualTo(HTTP_URL);
        assertThat(requestArgumentCaptor.getValue().method()).isEqualTo(GET_METHOD);
        assertThat(requestArgumentCaptor.getValue().headers()).hasSize(1);
        assertThat(requestArgumentCaptor.getValue().header(HEADER_KEY_CLIENT_TYPE)).isEqualTo(OK_HTTP);
    }

}
