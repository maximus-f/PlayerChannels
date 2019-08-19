package me.perotin.privatetalk.objects.inventory;

import java.util.List;

/* Created by Perotin on 8/16/19 */
public abstract class PagingMenu {

    private List<PrivateInventory> pages;
    private int pageNumber;
    /** Used in the title of every menu, for identifying what type of paging menu it is **/
    private String identifier;

    public PagingMenu(String identifier){
        this.identifier = identifier;
        this.pageNumber = 1;
        this.pages = null;
    }
    /**
     * Gives the next inventory in the sequence, null if it is the last inventory
     * @return the next inventory or null.
     */
    public PrivateInventory next(){
        if(pages.size() > pageNumber - 1){
            pageNumber++;
            return pages.get(pageNumber);
        } else throw new IndexOutOfBoundsException("Cannot use PrivateInventory.next when the next inventory does not exist!");
    }
    public PrivateInventory previous(){
        if(pageNumber > 1){
            pageNumber--;
            return pages.get(pageNumber);
        } else throw new IndexOutOfBoundsException("Cannot use PrivateInventory.previous when the previous inventory does not exist!");
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<PrivateInventory> getPages() {
        return pages;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Implement to the required design for each paging menu.
     * @return an inventory formatted specifically for each (#PagingMenu)
     */
    public abstract PrivateInventory getBlankInventory();

    public void setPages(List<PrivateInventory> pages){
        this.pages = pages;
    }

}
