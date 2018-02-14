package us.colloquy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter Gershkovich on 12/25/17.
 */
public class IndexSearchResult
{

   private List<Letter> letters = new ArrayList<>();

   private long numberOfResults;

    public List<Letter> getLetters()
    {
        return letters;
    }

    public void setLetters(List<Letter> letters)
    {
        this.letters = letters;
    }

    public long getNumberOfResults()
    {
        return numberOfResults;
    }

    public void setNumberOfResults(long numberOfResults)
    {
        this.numberOfResults = numberOfResults;
    }
}
