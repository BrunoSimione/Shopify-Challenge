package shopify.challenge;

/**
 *
 * @author Bruno Simione Beltrame
 */
public class Menu {
    private int id;
    private String data;
    private int parent_id;
    private int[] child_ids;
    
    public Menu(){
        
    }
    
    public int getId(){
        return this.id;
    }
    
    public int[] getChilds(){
       return this.child_ids;
    }
    
    public String getData(){
        return id + " " + data + " " + parent_id + " " + child_ids[0];
    }
    
    public String toString(){
        return String.valueOf(child_ids[0]);
    }
    
    public int getChild(int i){
        return child_ids[i];
    }
    
    public int getParent(){
        return parent_id;
    }
}
