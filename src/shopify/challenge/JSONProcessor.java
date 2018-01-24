package shopify.challenge;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Bruno Simiobe Beltrame
 * This class is responsible for all the process of retrieve input, filter and generate the output.
 * The class uses two data structures to filer the cyclical reference problem.
 *  One Hashmap with the menu's information
 *  Another with nodes to create the trees and store the parents/children.
 * The JSON output is been printed on the console.
 */

public class JSONProcessor {

    //Data Structures
    static HashMap<Integer, Menu> hmap = new HashMap<Integer, Menu>();
    static HashMap<Integer, Node<String>> hmapN = new HashMap<Integer, Node<String>>();

    //Auxiliar data structures
    static List<Menu> validMenus = new ArrayList<Menu>();
    static List<Menu> invalidMenus = new ArrayList<Menu>();

    public static void jsonManager(int id) {
        List<DataObject> pagesList = new ArrayList<DataObject>();
        DataObject firstPage = jsonGetRequest(id, 1);
        double totalItems = (double) firstPage.getPag().getTotal();
        double perPage = (double) firstPage.getPag().getPerPage();
        int pagesQtty = (int) Math.ceil(totalItems / perPage);
        pagesList.add(firstPage);

        //Loop through the pages
        for (int i = 2; i <= pagesQtty; i++) {
            DataObject page = jsonGetRequest(id, i);
            pagesList.add(page);
        }

        //System.out.println("Total: " + totalItems + "PP:" + perPage + "Qtty" + pagesQtty);
        //System.out.println(pagesList.size());

        filter(pagesList);
        generateOutput();
    }

    public static DataObject jsonGetRequest(int id, int page) {

        //Retrieve the JSON from the API/URL
        try {
            Reader reader = new InputStreamReader(new URL("https://backend-challenge-summer-2018.herokuapp.com/challenges.json?id=" + id + "&page=" + page).openStream()); //Read the json output
            Gson gson = new GsonBuilder().create();
            DataObject obj = gson.fromJson(reader, DataObject.class);

            return obj;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public static void filter(List<DataObject> menusList) {

        //The process validates all the menus and marks them as valid or invalid
        
        DataObject dobj;
        Menu currentMenu;

        //Loop through the pages and gets the menus
        for (int pageN = 0; pageN < menusList.size(); pageN++) {
            dobj = menusList.get(pageN);

            //Loop through each menu
            for (int i = 0; i < dobj.getMenus().size(); i++) {
                currentMenu = dobj.getMenus().get(i);
                boolean valid = true;

                Node<String> node = new Node<String>(String.valueOf(currentMenu.getId()));
                
                //Validates if is a root node
                if (currentMenu.getParent() == 0) {
                    //Add to the node's hash map as a root
                    hmapN.put(currentMenu.getId(), node);
                } else {
                    
                    //Add as a child of another node
                    findParent(currentMenu.getParent(), hmapN).addChild(node);
                    for (int j = 0; j < currentMenu.getChilds().length; j++) {
                        valid = true;
                        //Checks if the child references a parent node
                        if (hmap.containsKey(currentMenu.getChild(j))) {
                            valid = false;
                        }
                    }
                }
                if (valid) {
                    validMenus.add(currentMenu);
                } else {
                    invalidMenus.add(currentMenu);
                }

                //Add the menu to the Menu's hashmap (contains all informations)
                hmap.put(currentMenu.getId(), currentMenu);

            }


            
        }

        //Looping using Iterator - Used only to check the content of the Menu's hashmap
        /*
        Set set = hmap.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry) iterator.next();
        }

        for (Menu b : validMenus) {
            System.out.println(b.getId());
        }
        
        for (Menu c : invalidMenus) {
            System.out.println(c.getId());
        }
        */

    }

    //Find a node's parent
    public static Node findParent(int value, HashMap<Integer, Node<String>> map) {
        Node<String> returnedNode = null;
        for (Map.Entry m : map.entrySet()) {
            returnedNode = findNode((Node<String>) m.getValue(), value);
            if (returnedNode != null) {
                break;
            }
        }
        return returnedNode;
    }

    //Find a specific node using recursion
    public static Node findNode(Node<String> node, int value) {
        Node<String> returned = null;
        if (node.getData().equals(String.valueOf(value))) {
            returned = node;
        } else {
            for (int r = 0; r < node.getChildren().size(); r++) {
                Node child = node.getChild(r);
                if (child.getData().equals(String.valueOf(value))) {
                    returned = child;
                    break;
                } else {
                    returned = findNode(node.getChild(r), value);
                }
            }
        }
        return returned;
    }

    
    public static void generateOutput() {
        Node<String> rootNode;

        //Creates the list with all invalid menus
        List<List> totalInvalids = new ArrayList<List>();
        List menuInvalid;

        for (int x = 0; x < invalidMenus.size(); x++) {
            int nodeid = invalidMenus.get(x).getId();

            menuInvalid = new ArrayList();

            Set set = hmapN.entrySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry) iterator.next();
                rootNode = getRootParent((Node<String>) mentry.getValue());

                if (String.valueOf(mentry.getKey()).equals(rootNode.getData())) {
                    generateMenu(rootNode, menuInvalid);
                    hmapN.remove(Integer.parseInt(rootNode.getData()), rootNode);
                    hmap.remove(Integer.parseInt(rootNode.getData()));
                    break;
                }
            }

            totalInvalids.add(menuInvalid);
        }
   
        //Creates the list with all valid menu
        Set setV = hmapN.entrySet();
        Iterator iteratorV = setV.iterator();
        List<List> totalValids = new ArrayList<List>();
        while (iteratorV.hasNext()) {
            Map.Entry mentryV = (Map.Entry) iteratorV.next();
            Node<String> node = (Node<String>) mentryV.getValue();
            List individualList = new ArrayList<>();
            printJsonList(node, individualList);
            totalValids.add(individualList);
        }
        
        //Call the method to create the JSON
        createJson(totalInvalids, totalValids);

    }

    public static void createJson(List<List> invalids, List<List> valids) {
        JsonObject menus = new JsonObject();

        // create an array called datasets
        JsonArray datasetsInvalid = new JsonArray();
        JsonArray datasetsValid = new JsonArray();

        //Invalids List
        
        //Loop to create the invalid list
        for(int v = 0; v < invalids.size(); v++){
            List invalidArray = invalids.get(v);
            JsonObject datasetV = new JsonObject();
            
            int last = invalidArray.size() - 1;
            String root = String.valueOf(invalidArray.get(last));
            invalidArray.remove(last);
            
            datasetV.addProperty("root_id", root);
            
            Collections.sort(invalidArray);
            
            int[] ret = new int[invalidArray.size()];
            JsonArray arrayChildren = new JsonArray();
            for (int i=0; i < ret.length; i++)
            {
                ret[i] = Integer.parseInt(String.valueOf(invalidArray.get(i)));
                arrayChildren.add(ret[i]);
            }
            
            datasetV.add("children", arrayChildren);
  
            datasetsInvalid.add(datasetV);
        }
        
        //Valids Menus
        //Loop to create the valid list
        for(int v = 0; v < valids.size(); v++){
            List validArray = valids.get(v);
            JsonObject datasetI = new JsonObject();
            
            int last = validArray.size() - 1;
            String root = String.valueOf(validArray.get(0));
            validArray.remove(0);
            
            datasetI.addProperty("root_id", root);
            
            Collections.sort(validArray);
            
            int[] ret = new int[validArray.size()];
            JsonArray arrayChildren = new JsonArray();
            for (int i=0; i < ret.length; i++)
            {
                ret[i] = (int) validArray.get(i);
                arrayChildren.add(ret[i]);
            }
            
            datasetI.add("children", arrayChildren);
                                  
            datasetsValid.add(datasetI);
        }
        
        menus.add("valid_menus", datasetsValid);
        menus.add("invalid_menus", datasetsInvalid);
        
        

        // Create the gson using the GsonBuilder.
        // Serializing null and set all fields to the Upper Camel Case
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        System.out.println(gson.toJson(menus));
    }

    //Recursive method to add the node's values to the list
    public static void printJsonList(Node<String> node, List arrayList) {
        arrayList.add(Integer.parseInt(node.getData()));
        for (int a = 0; a < node.getChildren().size(); a++) {
            printJsonList(node.getChild(a), arrayList);
        }
    }

    //Find the root parent of any Node - Recursive
    public static Node<String> getRootParent(Node<String> node) {
        Node<String> nodeR = null;
        if (node.getParent() == null) {
            return node;
        } else {
            nodeR = getRootParent(node.getParent());
        }

        return nodeR;
    }

    public static void generateMenu(Node<String> startNode, List menuArray) {

        for (int r = 0; r < startNode.getChildren().size(); r++) {
            Node child = startNode.getChild(r);
            generateMenu(child, menuArray);
        }
        menuArray.add(Integer.parseInt(startNode.getData()));

    }

    //Only used to test the program
    public static void print() {
        Set set = hmapN.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry) iterator.next();
            System.out.println(mentry.getKey());

        }
    }

}
