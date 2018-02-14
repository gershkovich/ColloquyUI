package us.colloquy.tolstoy.client.ui.dataGrid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.user.cellview.client.DataGrid;

/**
 * Created by IntelliJ IDEA.
 * User: nm343
 * Date: 6/25/15
 * Time: 2:11 PM
 */
public interface DataGridResources extends DataGrid.Resources
{

	public DataGridResources INSTANCE =
			GWT.create( DataGridResources.class );

	/**
	 * The styles used in this widget.
	 */
	@ClientBundle.Source( "DataGrid.css" )
	DataGrid.Style dataGridStyle();
}
