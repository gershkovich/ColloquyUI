package us.colloquy.model.events;

/**
 * Created by Peter Gershkovich on 1/15/18.
 */
public class Source
{
    private String id;
    private String name;
    private String author;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }
}
