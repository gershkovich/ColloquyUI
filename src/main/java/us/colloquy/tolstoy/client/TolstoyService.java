package us.colloquy.tolstoy.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.ui.TextBox;
import us.colloquy.tolstoy.client.model.SearchFacets;
import us.colloquy.tolstoy.client.model.ServerResponse;

@RemoteServiceRelativePath("TolstoyService")
public interface TolstoyService extends RemoteService
{
    // Sample interface method of remote interface
    String submitComments(String email, String msg);

    ServerResponse getSelectedLetters(String searchElastic, SearchFacets searchFacets);

    ServerResponse getSelectedLettersWithOffset(int totalLettersLoaded, String searchCriteria, SearchFacets searchFacets);

    ServerResponse getDataForCharts(SearchFacets searchFacets);

    ServerResponse getLettersSubset(String range, String searchString, SearchFacets searchFacets);

    /**
     * Utility/Convenience class.
     * Use TolstoyService.App.getInstance() to access static instance of TolstoyServiceAsync
     */
    public static class App
    {
        private static TolstoyServiceAsync ourInstance = GWT.create(TolstoyService.class);

        public static synchronized TolstoyServiceAsync getInstance()
        {
            return ourInstance;
        }
    }
}
