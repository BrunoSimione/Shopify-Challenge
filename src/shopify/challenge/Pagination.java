package shopify.challenge;

/**
 *
 * @author Bruno Simione Beltrame
 */
public class Pagination {
    private int current_page;
    private int per_page;
    private int total;
    
    @Override
    public String toString(){
        return current_page + " " + per_page + " " + total;
    }
    
    public int getCurrentPage(){
        return this.current_page;
    }
    
    public int getPerPage(){
        return this.per_page;
    }
    
    public int getTotal(){
        return this.total;
    }
}
