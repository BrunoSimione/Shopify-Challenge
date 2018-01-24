package shopify.challenge;

import java.util.List;

/**
 *
 * @author Bruno Simione Beltrame
 */
public class DataObject {
    private List<Menu> menus;
    private Pagination pagination;
    
    public List<Menu> getMenus(){
        return menus;
    }
    
    public Pagination getPag(){
        return pagination;
    }
}
