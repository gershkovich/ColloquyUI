package us.colloquy.tolstoy.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter Gershkovich on 12/24/17.
 */
public class ServerResponse implements Serializable
{
    private List<LetterDisplay> letters = new ArrayList<>();
    private String feedback;
    private long totalNumberOfLetters;
    private String selectedStats;
    private String workEvents;

    private String csvLetterData;

    public List<LetterDisplay> getLetters()
    {
        return letters;
    }

    public void setLetters(List<LetterDisplay> letters)
    {
        this.letters = letters;
    }

    public String getFeedback()
    {
        return feedback;
    }

    public void setFeedback(String feedback)
    {
        this.feedback = feedback;
    }

    public long getTotalNumberOfLetters()
    {
        return totalNumberOfLetters;
    }

    public void setTotalNumberOfLetters(long totalNumberOfLetters)
    {
        this.totalNumberOfLetters = totalNumberOfLetters;
    }

    public String getCsvLetterData()
    {
        return csvLetterData;
    }

    public void setCsvLetterData(String csvLetterData)
    {
        this.csvLetterData = csvLetterData;
    }

    public String getSelectedStats()
    {
        return selectedStats;
    }

    public void setSelectedStats(String selectedStats)
    {
        this.selectedStats = selectedStats;
    }

    public String getWorkEvents()
    {
        return workEvents;
    }

    public void setWorkEvents(String workEvents)
    {
        this.workEvents = workEvents;
    }
}
