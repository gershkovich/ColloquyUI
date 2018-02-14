package us.colloquy.tolstoy.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter Gershkovich on 2/12/18.
 */
public class SearchFacets implements Serializable
{
    List<String> indexesList = new ArrayList<>();

    public List<String> getIndexesList()
    {
        return indexesList;
    }

    public void setIndexesList(List<String> indexesList)
    {
        this.indexesList = indexesList;
    }
}
