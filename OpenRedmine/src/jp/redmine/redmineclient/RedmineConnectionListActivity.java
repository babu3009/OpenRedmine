package jp.redmine.redmineclient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jp.redmine.redmineclient.db.RedmineConnectionModel;
import jp.redmine.redmineclient.entity.RedmineConnection;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RedmineConnectionListActivity extends Activity {
	static final int DIALOG_ITEM_ACTION = 0;
	static final int DIALOG_CONFIRM_DELETE = 1;
	static final String DIALOG_PARAM_ID = "ID";
	static final String DIALOG_PARAM_NAME = "NAME";
	private NotificationManager notifManager;
	private ArrayAdapter<RedmineConnection> listAdapter;



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connectionlist);

		notifManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);


		ListView list = (ListView)findViewById(R.id.listConnectionList);
		listAdapter = new ArrayAdapter<RedmineConnection>(
				this,android.R.layout.simple_list_item_1
				,new ArrayList<RedmineConnection>());

		list.setAdapter(listAdapter);

		onReload();

		//リスト項目がクリックされた時の処理
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
				ListView listView = (ListView) parent;
				RedmineConnection item = (RedmineConnection) listView.getItemAtPosition(position);
				onItemSelect(item.getId());
			}
		});

		//リスト項目が長押しされた時の処理
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				RedmineConnection item = (RedmineConnection) listView.getItemAtPosition(position);
				Bundle bundle = new Bundle();
				bundle.putInt(DIALOG_PARAM_ID, item.getId());
				bundle.putString(DIALOG_PARAM_NAME, item.getName());
				showDialog(DIALOG_ITEM_ACTION, bundle);
				return true;
			}
		});
	}

	protected void onItemSelect(int id){
		Intent intent = new Intent( getApplicationContext(), RedmineProjectListActivity.class );
		intent.putExtra(RedmineProjectListActivity.INTENT_INT_CONNECTION_ID, id);
		startActivity( intent );
	}
	@Override
	protected void onResume() {
		super.onResume();
		onReload();
	}

	protected void onReload(){
		(new SelectDataTask()).execute("");
	}

	protected void onItemNew(){
		onItemEdit(-1);
	}
	protected void onItemEdit(int itemid){
		Intent intent = new Intent( getApplicationContext(), RedmineConnectionActivity.class );
		intent.putExtra(RedmineConnectionActivity.INTENT_INT_ID, itemid);
		startActivity( intent );
	}

	protected void onItemDelete(int itemid){
		RedmineConnectionModel model = new RedmineConnectionModel(this);
		try {
			model.delete(itemid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		onReload();
	}

	@Override
	protected Dialog onCreateDialog(int id,final Bundle arg) {
		Dialog dialog = null;
		Notification notif = new Notification();
		notif.vibrate = new long[]{50,100};
		notifManager.notify(R.string.app_name, notif);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(String.format(
				this.getString(R.string.menu_setting_list_menu_title)
				, arg.getString(DIALOG_PARAM_NAME)));
		switch(id) {
		case DIALOG_ITEM_ACTION:

			final CharSequence[] items = {
				 this.getString(R.string.menu_setting_list_menu_edit)
				,this.getString(R.string.menu_setting_list_menu_delete)
				};
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch(item){
					case 1:
						dialog.cancel();
						showDialog(DIALOG_CONFIRM_DELETE, arg);
						break;
					case 0:
						dialog.cancel();
						onItemEdit(arg.getInt(DIALOG_PARAM_ID));
						break;
					default:
					}
				}
			});
			dialog = builder.create();

			break;
		case DIALOG_CONFIRM_DELETE:
			builder.setMessage(getString(R.string.menu_confirm_delete_msg))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.menu_config_delete_yes)
						, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						onItemDelete(arg.getInt(DIALOG_PARAM_ID));
					}
				})
				.setNegativeButton(getString(R.string.menu_config_delete_no)
						, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
			dialog = builder.create();
			break;
		default:
			break;
		}
		return dialog;
	}



	private class SelectDataTask extends AsyncTask<String, Integer, List<RedmineConnection>> {
		@Override
		protected List<RedmineConnection> doInBackground(String ... params) {
			RedmineConnectionModel model = new RedmineConnectionModel(getBaseContext());
			List<RedmineConnection> con = new ArrayList<RedmineConnection>();
			try {
				con = model.fetchAll();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return con;
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(List<RedmineConnection> b) {
			listAdapter.notifyDataSetInvalidated();
			listAdapter.clear();
			for (RedmineConnection i : b){
				listAdapter.add(i);
			}
			listAdapter.notifyDataSetChanged();

		}

	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu( menu );

		MenuInflater inflater = getMenuInflater();
		inflater.inflate( R.menu.connection, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch ( item.getItemId() )
		{
			case R.id.menu_access_addnew:
			{
				onItemNew();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
}