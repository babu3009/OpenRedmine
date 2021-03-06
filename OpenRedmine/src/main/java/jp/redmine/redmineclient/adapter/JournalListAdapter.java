package jp.redmine.redmineclient.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import jp.redmine.redmineclient.R;
import jp.redmine.redmineclient.activity.handler.WebviewActionInterface;
import jp.redmine.redmineclient.adapter.form.IssueJournalHeaderForm;
import jp.redmine.redmineclient.adapter.form.IssueJournalItemForm;
import jp.redmine.redmineclient.db.cache.DatabaseCacheHelper;
import jp.redmine.redmineclient.db.cache.RedmineCategoryModel;
import jp.redmine.redmineclient.db.cache.RedmineJournalModel;
import jp.redmine.redmineclient.db.cache.RedminePriorityModel;
import jp.redmine.redmineclient.db.cache.RedmineProjectModel;
import jp.redmine.redmineclient.db.cache.RedmineStatusModel;
import jp.redmine.redmineclient.db.cache.RedmineTrackerModel;
import jp.redmine.redmineclient.db.cache.RedmineUserModel;
import jp.redmine.redmineclient.db.cache.RedmineVersionModel;
import jp.redmine.redmineclient.entity.DummySelection;
import jp.redmine.redmineclient.entity.IMasterRecord;
import jp.redmine.redmineclient.entity.RedmineJournal;
import jp.redmine.redmineclient.entity.RedmineJournalChanges;
import jp.redmine.redmineclient.entity.TypeConverter;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

class JournalListAdapter extends RedmineDaoAdapter<RedmineJournal, Long, DatabaseCacheHelper>  implements StickyListHeadersAdapter {
	private final static String TAG = JournalListAdapter.class.getSimpleName();

	private RedmineJournalModel mJournal;
	private RedmineVersionModel mVersion;
	private RedmineUserModel mUser;
	private RedmineStatusModel mStatus;
	private RedmineCategoryModel mCategory;
	private RedmineTrackerModel mTracker;
	private RedminePriorityModel mPriority;
	private RedmineProjectModel mProject;
	protected Integer connection_id;
	protected Long issue_id;
	protected Long project_id;
	protected WebviewActionInterface action;
	protected HashMap<String,fetchHelper> fetchMap = new HashMap<String, JournalListAdapter.fetchHelper>();

	protected void setupHashmap(){
		fetchMap.put("is_private", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) {
				DummySelection item = new DummySelection();
				item.setName(input);
				return item;
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_private;
			}
		});
		fetchMap.put("done_ratio", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) {
				DummySelection item = new DummySelection();
				item.setName(input + "%");
				return item;
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_progress;
			}
		});
		fetchMap.put("estimated_hours", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) {
				DummySelection item = new DummySelection();
				item.setName(input);
				return item;
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_time;
			}
		});
		fetchMap.put("due_date", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) {
				DummySelection item = new DummySelection();
				item.setName(input);
				return item;
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_date_due;
			}
		});
		fetchMap.put("subject", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) {
				DummySelection item = new DummySelection();
				item.setName(input);
				return item;
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_title;
			}
		});
		fetchMap.put("start_date", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) {
				DummySelection item = new DummySelection();
				item.setName(input);
				return item;
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_date_start;
			}
		});
		fetchMap.put("fixed_version_id", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) throws SQLException {
				if(connection_id == null)
					return null;
				return mVersion.fetchById(connection_id, TypeConverter.parseInteger(input));
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_version;
			}
		});
		fetchMap.put("assigned_to_id", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) throws SQLException {
				if(connection_id == null)
					return null;
				return mUser.fetchById(connection_id, TypeConverter.parseInteger(input));
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_assigned;
			}
		});
		fetchMap.put("status_id", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) throws SQLException {
				if(connection_id == null)
					return null;
				return mStatus.fetchById(connection_id, TypeConverter.parseInteger(input));
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_status;
			}
		});
		fetchMap.put("category_id", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) throws SQLException {
				if(connection_id == null)
					return null;
				return mCategory.fetchById(connection_id, TypeConverter.parseInteger(input));
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_category;
			}
		});
		fetchMap.put("tracker_id", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) throws SQLException {
				if(connection_id == null)
					return null;
				return mTracker.fetchById(connection_id, TypeConverter.parseInteger(input));
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_tracker;
			}
		});
		fetchMap.put("priority_id", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) throws SQLException {
				if(connection_id == null)
					return null;
				return mPriority.fetchById(connection_id, TypeConverter.parseInteger(input));
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_priority;
			}
		});
		fetchMap.put("project_id", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) throws SQLException {
				if(connection_id == null)
					return null;
				return mProject.fetchById(connection_id, TypeConverter.parseInteger(input));
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_project;
			}
		});
		fetchMap.put("attachment", new fetchHelper(){
			@Override
			protected IMasterRecord getRawItem(String input) throws SQLException {
				DummySelection item = new DummySelection();
				item.setName(input);
				return item;
			}
			@Override
			public int getResourceNameId() {
				return R.string.ticket_attachments;
			}
		});
	}

	private abstract class fetchHelper{
		abstract protected IMasterRecord getRawItem(String input) throws SQLException;
		abstract public int getResourceNameId();
		public IMasterRecord getItem(String input) throws SQLException{
			if(TextUtils.isEmpty(input))
				return null;
			return getRawItem(input);
		}
	}
	public JournalListAdapter(DatabaseCacheHelper m, Context context, WebviewActionInterface act) {
		super(m, context, RedmineJournal.class);
		mJournal = new RedmineJournalModel(m);
		mVersion = new RedmineVersionModel(m);
		mUser = new RedmineUserModel(m);
		mStatus = new RedmineStatusModel(m);
		mCategory = new RedmineCategoryModel(m);
		mTracker  = new RedmineTrackerModel(m);
		mPriority = new RedminePriorityModel(m);
		mProject = new RedmineProjectModel(m);
		action = act;
		setupHashmap();
	}


	public void setupParameter(int connection, long project , long issue){
		connection_id = connection;
		issue_id = issue;
		project_id = project;
	}

    @Override
	public boolean isValidParameter(){
		if(issue_id == null || connection_id == null)
			return false;
		else
			return true;
	}

	@Override
	protected int getItemViewId() {
		return R.layout.listitem_journal;
	}

	@Override
	protected void setupView(View view, RedmineJournal data) {
		IssueJournalItemForm form;
		if(view.getTag() != null && view.getTag() instanceof IssueJournalItemForm){
			form = (IssueJournalItemForm)view.getTag();
		} else {
			form = new IssueJournalItemForm(view);
			form.setupWebView(action);
		}
		form.setValue(data, project_id);
	}

	@Override
	protected RedmineJournal getDbItem(int position) {
		RedmineJournal item = super.getDbItem(position);
		try {
			item.changes = item.getDetails();
			for(RedmineJournalChanges cg : item.changes){
				if("attr".equalsIgnoreCase(cg.getProperty()))
					getAttributeDetail(cg);
				else if("attachment".equalsIgnoreCase(cg.getProperty()))
					getAttachmentDetail(cg);
				else
					Log.w(TAG,"Changes: " + cg.getName() + "," + cg.getProperty());
			}
		} catch (IOException e) {
			Log.e(TAG,"getDbItem",e);
		} catch (ClassNotFoundException e) {
			Log.e(TAG,"getDbItem",e);
		} catch (SQLException e) {
			Log.e(TAG, "getDbItem", e);
		}
		return item;
	}
	@Override
	protected QueryBuilder<RedmineJournal, Long> getQueryBuilder() throws SQLException {
		QueryBuilder<RedmineJournal, Long> builder = dao.queryBuilder();
		Where<RedmineJournal,Long> where = builder.where()
				.eq(RedmineJournal.CONNECTION, connection_id)
				.and()
				.eq(RedmineJournal.ISSUE_ID, issue_id)
				;
		builder.setWhere(where);
		builder.orderBy(RedmineJournal.JOURNAL_ID, true);
		return builder;
	}

	protected void getAttributeDetail(RedmineJournalChanges cg) throws SQLException{
		if(TextUtils.isEmpty(cg.getName()))
			return;
		String name = cg.getName();
		if(!fetchMap.containsKey(name)){
			Log.w(TAG,"Undefined key: " + name + "," + cg.getProperty());
			return;
		}
		fetchHelper helper = fetchMap.get(name);
		cg.setResourceId(helper.getResourceNameId());
		cg.setMasterBefore(helper.getItem(cg.getBefore()));
		cg.setMasterAfter(helper.getItem(cg.getAfter()));
	}
	protected void getAttachmentDetail(RedmineJournalChanges cg) throws SQLException{
		if(TextUtils.isEmpty(cg.getName()))
			return;
		//String name = cg.getName();
		fetchHelper helper = fetchMap.get("attachment");
		cg.setResourceId(helper.getResourceNameId());
		cg.setMasterBefore(helper.getItem(cg.getBefore()));
		cg.setMasterAfter(helper.getItem(cg.getAfter()));
	}
	@Override
	protected long getDbItemId(RedmineJournal item) {
		if(item == null){
			return -1;
		} else {
			return item.getId();
		}
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView != null && (
				 convertView.getTag() == null
				|| ! ( (Integer)(convertView.getTag()) != R.layout.listheader_journal)
			)) {
			convertView = null;
		}
		if (convertView == null) {
			convertView = infrator.inflate(R.layout.listheader_journal, null);
			convertView.setTag(R.layout.listheader_journal);
		}
		if(convertView != null){
			RedmineJournal rec = getDbItem(position);
			IssueJournalHeaderForm form = new IssueJournalHeaderForm(convertView);
			form.setValue(rec);
		}
		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		return getItemId(position);
	}


}
