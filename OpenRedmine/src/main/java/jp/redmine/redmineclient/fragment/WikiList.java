package jp.redmine.redmineclient.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import android.support.v4.widget.ListFragmentSwipeRefreshLayout;
import com.j256.ormlite.android.apptools.OrmLiteListFragment;

import jp.redmine.redmineclient.R;
import jp.redmine.redmineclient.activity.handler.WebviewActionInterface;
import jp.redmine.redmineclient.adapter.WikiListAdapter;
import jp.redmine.redmineclient.db.cache.DatabaseCacheHelper;
import jp.redmine.redmineclient.entity.RedmineConnection;
import jp.redmine.redmineclient.entity.RedmineWiki;
import jp.redmine.redmineclient.fragment.helper.ActivityHandler;
import jp.redmine.redmineclient.model.ConnectionModel;
import jp.redmine.redmineclient.param.ProjectArgument;
import jp.redmine.redmineclient.task.SelectWikiTask;

public class WikiList extends OrmLiteListFragment<DatabaseCacheHelper> implements SwipeRefreshLayout.OnRefreshListener {
	private static final String TAG = WikiList.class.getSimpleName();
	private WikiListAdapter adapter;
	private SelectDataTask task;
	private View mFooter;
	private MenuItem menu_refresh;
	SwipeRefreshLayout mSwipeRefreshLayout;

	private WebviewActionInterface mListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = ActivityHandler.getHandler(activity, WebviewActionInterface.class);

	}
	protected void cancelTask(){
		// cleanup task
		if(task != null && task.getStatus() == AsyncTask.Status.RUNNING){
			task.cancel(true);
		}
	}
	public WikiList(){
		super();
	}

	static public WikiList newInstance(ProjectArgument arg){
		WikiList fragment = new WikiList();
		fragment.setArguments(arg.getArgument());
		return fragment;
	}

	@Override
	public void onDestroyView() {
		cancelTask();
		setListAdapter(null);
		super.onDestroyView();
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setFastScrollEnabled(true);

		adapter = new WikiListAdapter(getHelper(), getActivity());
		ProjectArgument intent = new ProjectArgument();
		intent.setArgument(getArguments());
		adapter.setupParameter(intent.getConnectionId(),intent.getProjectId());
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();

        if(adapter.getCount() == 0)
            onRefresh();

	}

	@Override
	public void onResume() {
		super.onResume();
		if(adapter != null)
			adapter.notifyDataSetChanged();
	}

	@Override
	public void onListItemClick(ListView listView, View v, int position, long id) {
		super.onListItemClick(listView, v, position, id);
		Object listitem = listView.getItemAtPosition(position);
		if(listitem == null || !RedmineWiki.class.isInstance(listitem)  )
		{
			return;
		}
		RedmineWiki item = (RedmineWiki) listitem;
		mListener.wiki(item.getConnectionId(),item.getProject().getId(),item.getTitle());
	}

	private class SelectDataTask extends SelectWikiTask {
		public SelectDataTask(DatabaseCacheHelper helper, RedmineConnection con, long proj_id){
			super(helper,con,proj_id);
		}

		// can use UI thread here
		@Override
		protected void onPreExecute() {
			mFooter.setVisibility(View.VISIBLE);
			if(menu_refresh != null)
				menu_refresh.setEnabled(false);
			if(mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing())
				mSwipeRefreshLayout.setRefreshing(true);
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(Void b) {
			mFooter.setVisibility(View.GONE);
			adapter.notifyDataSetChanged();
			if(menu_refresh != null)
				menu_refresh.setEnabled(true);
			if(mSwipeRefreshLayout != null)
				mSwipeRefreshLayout.setRefreshing(false);
		}

		@Override
		protected void onProgress(int max, int proc) {
			adapter.notifyDataSetChanged();
			super.onProgress(max, proc);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mFooter = inflater.inflate(R.layout.listview_footer,null);
		mFooter.setVisibility(View.GONE);
		View view = super.onCreateView(inflater, container, savedInstanceState);
		mSwipeRefreshLayout = ListFragmentSwipeRefreshLayout.inject(container, view);
		mSwipeRefreshLayout.setOnRefreshListener(this);
		return mSwipeRefreshLayout;
	}
	public void onRefresh(){
		if(task != null && task.getStatus() == AsyncTask.Status.RUNNING){
			return;
		}
		ProjectArgument intent = new ProjectArgument();
		intent.setArgument(getArguments());
		int id = intent.getConnectionId();
		ConnectionModel mConnection = new ConnectionModel(getActivity());
		RedmineConnection connection = mConnection.getItem(id);
		mConnection.finalize();
		task = new SelectDataTask(getHelper(), connection, (long)intent.getProjectId());
		task.execute("");
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.refresh, menu);
		menu_refresh = menu.findItem(R.id.menu_refresh);
		if(task != null && task.getStatus() == AsyncTask.Status.RUNNING)
			menu_refresh.setEnabled(false);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch ( item.getItemId() )
		{
			case R.id.menu_refresh:
			{
				this.onRefresh();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
}
