@Path("/")
public class CookieResource {
    
    private static final Logger log = LoggerFactory.getLogger(CookieResource.class);
    private static final String cookieIdKey = "cookieId";
    private static final String dspIdKey = "dsp-id";
    private static final String doNotForwardKey = "dnf";
    private static final Integer doNotForwardValue = 1;
    private static final int FOUND = 302;
    private final URI dspURI;
    private final URI secureURI;
    private final URI optoutLandingURI;
    private final boolean testMode;
    private final ActiveMQManagedClient activeMQManagedClient;
    private final BufferedImage pixel;
    
    
    
    public CookieResource(CookieAppConfiguration config, ActiveMQManagedClient activeMQManagedClient, BufferedImage pixel) {
   	 this.activeMQManagedClient = activeMQManagedClient;
   	 this.dspURI = URI.create(config.getRedirectUri());
   	 this.secureURI = URI.create(config.getRedirectSecureUri());
   	 this.optoutLandingURI = URI.create(config.getOptoutLandingUri());
   	 this.testMode = config.getTestMode();
   	 this.pixel = pixel;
    }
    
    
    @GET
    @Path("rtb/optout")
    @Timed
    public Response cookieOptout(@CookieParam(cookieIdKey) String cookieId) {
   	 if(cookieId != null) {
   		 activeMQManagedClient.queueOptOutCookie(cookieId);
   	 }
   	 return Response.status(FOUND).location(optoutLandingURI).build();
    }
    
    
    @GET
    @Path("rtb/dsp/cookie-mapper")
    @Timed
    public Response mapCookies(@QueryParam(dspIdKey) String dspId,
   		 @CookieParam(cookieIdKey) String cookieId,
   		 @QueryParam(doNotForwardKey) Integer doNotForward,
   		 @Context HttpServletRequest req, @Context HttpServletResponse res) {
   	 
   	 if(dspId != null && cookieId != null || testMode){
   		 activeMQManagedClient.queueMappedCookie(cookieId, dspId);
   		 if(doNotForward != null && doNotForward.equals(doNotForwardValue)) {
   			 addPixelToResponse(res);
   			 return Response.ok().build(); // and send a transparent pixel
   		 }
   	 }
   	 if(req.isSecure()){
   		 return Response.status(FOUND).location(secureURI).build();
   	 }else {
   		 return Response.status(FOUND).location(dspURI).build();
   	 }
   	 
    }
    
    
    private void addPixelToResponse(HttpServletResponse res) {
   	 try{
   		 res.setHeader("Content-Type", "image/gif");
        	res.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        	res.setHeader("Pragma", "no-cache");
        	res.setDateHeader("Expires", 0);
   		 OutputStream output = res.getOutputStream();
   		 ImageIO.write(pixel, "png", output);
   	 }catch(Exception e) {
   		 log.error("ERROR while adding pixel to response: " + e.getMessage());
   	 }
   	 
   	 
    }
    
    
    
    
}
