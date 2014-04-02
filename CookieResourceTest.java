public class CookieResourceTest {

    
    @Test
    public void cookieOptoutTest() {
   	 CookieAppConfiguration config = mock(CookieAppConfiguration.class);
   	 when(config.getOptoutLandingUri()).thenReturn("http://www.test.com");
   	 when(config.getDSPRedirectSecureUri()).thenReturn("https://www.test.com");
   	 when(config.getDSPRedirectUri()).thenReturn("http://www.test.com");
   	 ActiveMQManagedClient mqClient = mock(ActiveMQManagedClient.class);
   	 BufferedImage pixel = mock(BufferedImage.class);
   	 CookieResource resource = new CookieResource(config, mqClient, pixel);
   	 String cookieId = "cookieId";
   	 Response response =  resource.cookieOptout(cookieId);
   	 String redirectUri = response.getMetadata().getFirst("location").toString();
   	 assertTrue("response statuses didn't match",response.getStatus() == 302);
   	 assertTrue("redirection Uri doesn't match",redirectUri.equals(config.getOptoutLandingUri()));
   	 verify(mqClient).queueOptOutCookie(cookieId);
    }
    
    
    @Test
    public void cookieOptoutWithNotCookieIdTest() {
   	 CookieAppConfiguration config = mock(CookieAppConfiguration.class);
   	 when(config.getOptoutLandingUri()).thenReturn("http://www.test.com");
   	 when(config.getDSPRedirectSecureUri()).thenReturn("https://www.test.com");
   	 when(config.getDSPRedirectUri()).thenReturn("http://www.test.com");
		 ActiveMQManagedClient mqClient = mock(ActiveMQManagedClient.class);
   	 BufferedImage pixel = mock(BufferedImage.class);
   	 CookieResource resource = new CookieResource(config, mqClient, pixel);
   	 String cookieId = null;
   	 Response response = resource.cookieOptout(cookieId);
   	 String redirectUri = response.getMetadata().getFirst("location").toString();
   	 assertTrue("response statuses didn't match",response.getStatus() == 302);
   	 assertTrue("redirect Uri doesn't match",redirectUri.equals(config.getOptoutLandingUri()));
   	 verifyZeroInteractions(mqClient);
    }
    
    
    
    
    
    
}