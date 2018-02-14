package us.colloquy.tolstoy.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import us.colloquy.tolstoy.client.model.SearchFacets;
import us.colloquy.tolstoy.client.model.ServerResponse;

public interface TolstoyServiceAsync
{
    void submitComments(String email, String msg, AsyncCallback<String> async);

    void getSelectedLetters(String searchElastic, SearchFacets searchFasset, AsyncCallback<ServerResponse> async);

    void getSelectedLettersWithOffset(int totalLettersLoaded, String searchCriteria, SearchFacets searchFacets, AsyncCallback<ServerResponse> async);

    void getDataForCharts( SearchFacets searchFacets, AsyncCallback<ServerResponse> async);

    void getLettersSubset(String range, String searchString, SearchFacets searchFacets, AsyncCallback<ServerResponse> async);
}
