package com.example.tulingdemo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tulingdemo.ChatThread.MessageListener;
/**
 * 聊天主界面
 */
public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private static final String ENDPOINT = "http://www.tuling123.com/openapi/api";
	private static final String APPKEY = "d545d3de184f5ae82f1090b83f965571";
	private static final String KEY = "key";
	private static final String INFO = "info";
	private static final String USERID = "userid";

	List<ChatMessage> lists;
	private ListView mListView;
	private EditText mEditText;
	private Button mButton;
	private TextAdapter mTextAdapter;
	private String[] welcome_array;
	private double currentTime, oldTime = 0;
	private ChatThread mChatThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	/**
	 * 初始化视图
	 */
	private void initView() {
		mListView = (ListView) findViewById(R.id.lv);
		mEditText = (EditText) findViewById(R.id.sendText);
		mButton = (Button) findViewById(R.id.sendBtn);
		lists = new ArrayList<ChatMessage>();
		/**
		 * 设置聊天工作线程
		 */
		mChatThread = new ChatThread(new Handler());
		mChatThread.setListener(new MessageListener() {
			
			@Override
			public void onChatMessage(String revMsg) {
				parseJson(revMsg);
			}
		});
		mChatThread.start();
		mChatThread.getLooper();
		
		// 为发送按钮设置监听器
		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getTime();
				// 获取发送的内容
				String content_text = formatContent(mEditText.getText()
						.toString());
				mEditText.setText("");
				
				// 将用户发送的数据放入列表中
				ChatMessage chatMessage = new ChatMessage(content_text, ChatMessage.SEND,
						getTime());
				lists.add(chatMessage);
				/*
				 *  当lists数据超过30条移除十条记录
				 *  这里需要注意下List集合是顺序列表循环移除只需把第0项移除即可
				 */
				if (lists.size() > 30) {
					for (int i = 0; i < 10; i++) {
						lists.remove(0);
					}
				}
				// 数据刷新
				dataSetChanged();
				// 将发送的消息传入消息工作线程，加入消息队列并等待返回
				mChatThread.queueMessage(construcUrl(content_text));
			}
		});
		// ListView绑定数据适配器
		mTextAdapter = new TextAdapter();
		mListView.setAdapter(mTextAdapter);
		// 为lists添加欢迎语
		lists.add(new ChatMessage(getRandomWelcomeTips(), ChatMessage.RECEIVER,
				getTime()));
	}

	/**
	 * 获取显示时间
	 */
	private String getTime() {
		currentTime = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		Date curDate = new Date();
		String curStr = format.format(curDate);
		if (currentTime - oldTime > 1 * 60 * 1000) {
			oldTime = currentTime;
			return curStr;
		} else {
			oldTime = currentTime;
			return null;
		}
	}

	/**
	 * 去掉发送空格和回车符
	 */
	private String formatContent(String content) {
		return content.replace(" ", "").replace("\n", "");
	}

	/**
	 * 欢迎语获取
	 */
	private String getRandomWelcomeTips() {
		String welcome_tip = null;
		welcome_array = this.getResources()
				.getStringArray(R.array.welcome_tips);
		int index = (int) (Math.random() * welcome_array.length);
		welcome_tip = welcome_array[index];
		return welcome_tip;
	}

	/**
	 * 解析json数据
	 */
	private void parseJson(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			ChatMessage chatMessage;
			chatMessage = new ChatMessage(jsonObject.getString("text"),
					ChatMessage.RECEIVER, getTime());
			lists.add(chatMessage);
			// 数据刷新
			dataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新ListView
	 */
	private void dataSetChanged() {
		mTextAdapter.notifyDataSetChanged();
	}

	/**
	 * ListView数据适配器
	 */
	private class TextAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return lists.size();
		}

		@Override
		public Object getItem(int position) {
			return lists.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public int getItemViewType(int position) {
			ChatMessage ChatMessage = lists.get(position);
			if (ChatMessage.getFlag()==ChatMessage.SEND) {
				return 0;
			}
			return 1;
		}

		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			ViewHolder viewHolder = null;
			// 判断是接收方还是发送方
			if (null == convertView) {
				if (getItemViewType(position) == ChatMessage.RECEIVER) {
					convertView = inflater.inflate(R.layout.leftitem, null);
					viewHolder = new ViewHolder();
					viewHolder.textView = (TextView) convertView
							.findViewById(R.id.textView);
					viewHolder.timeView = (TextView) convertView
							.findViewById(R.id.timeTextView);
				} else if (getItemViewType(position) == ChatMessage.SEND) {
					convertView = inflater.inflate(R.layout.rightitem, null);
					viewHolder = new ViewHolder();
					viewHolder.textView = (TextView) convertView
							.findViewById(R.id.textView);
					viewHolder.timeView = (TextView) convertView
							.findViewById(R.id.timeTextView);
				}
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.textView.setText(lists.get(position).getContent());
			viewHolder.timeView.setText(lists.get(position).getTime());

			return convertView;
		}
	}

	/**
	 * ViewHolder复用视图
	 */
	private final class ViewHolder {
		TextView textView;
		TextView timeView;
	}

	/**
	 * 构造URL
	 */
	private String construcUrl(String content) {
		String url = Uri.parse(ENDPOINT).buildUpon()
				.appendQueryParameter(KEY, APPKEY)
				.appendQueryParameter(USERID, "1")
				.appendQueryParameter(INFO, content).build().toString();
		return url;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mChatThread.clearQueue();
		mChatThread.quit();
	}
}
