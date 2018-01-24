package shopify.challenge;

import java.io.IOException;

/**
 * @author Bruno Simione Beltrame
 */
public class ShopifyChallenge {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        //First Challenge / id = 1
        JSONProcessor.jsonManager(1);
        
        //Second Challenge / id = 2
        JSONProcessor.jsonManager(2);
    }
    
}
