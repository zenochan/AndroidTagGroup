package me.gujun.android.taggroup.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import me.gujun.android.taggroup.TagGroup;
import me.gujun.android.taggroup.demo.db.TagsManager;


public class MainActivity extends AppCompatActivity
{
  private TextView mPromptText;

  private TagGroup testTagGroup;
  private TagGroup mDefaultTagGroup;
  private TagGroup mSmallTagGroup;
  private TagGroup mLargeTagGroup;
  private TagGroup mBeautyTagGroup;
  private TagGroup mBeautyInverseTagGroup;
  private TagGroup tagSingleChoose;
  private TagGroup tagMultiChoose;

  private TagsManager mTagsManager;

  private Function1<String, Unit> mTagClickListener = new Function1<String, Unit>()
  {
    @Override public Unit invoke(String s)
    {
      Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
      return Unit.INSTANCE;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mTagsManager = TagsManager.getInstance(getApplicationContext());
    String[] tags = new String[]{"test", "tags"};

    mPromptText = (TextView) findViewById(R.id.tv_prompt);
    mPromptText.setVisibility((tags == null || tags.length == 0) ? View.VISIBLE : View.GONE);
    mPromptText.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        launchTagEditorActivity();
      }
    });

    testTagGroup = findViewById(R.id.tag_test);
    mDefaultTagGroup = findViewById(R.id.tag_group);
    mSmallTagGroup = findViewById(R.id.tag_group_small);
    mLargeTagGroup = findViewById(R.id.tag_group_large);
    mBeautyTagGroup = findViewById(R.id.tag_group_beauty);
    mBeautyInverseTagGroup = findViewById(R.id.tag_group_beauty_inverse);
    tagSingleChoose = findViewById(R.id.tag_group_single_choose);
    tagMultiChoose = findViewById(R.id.tag_group_multi_choose);
    if (tags != null && tags.length > 0) {
      mDefaultTagGroup.setTags(tags);
      mSmallTagGroup.setTags(tags);
      mLargeTagGroup.setTags(tags);
      mBeautyTagGroup.setTags(tags);
      mBeautyInverseTagGroup.setTags(tags);
      testTagGroup.setTags(tags);
      tagSingleChoose.setTags(tags);
      tagMultiChoose.setTags(tags);
    }


    MyTagGroupOnClickListener tgClickListener = new MyTagGroupOnClickListener();

    mDefaultTagGroup.setOnClickListener(tgClickListener);
    mSmallTagGroup.setOnClickListener(tgClickListener);
    mLargeTagGroup.setOnClickListener(tgClickListener);
    mBeautyTagGroup.setOnClickListener(tgClickListener);
    mBeautyInverseTagGroup.setOnClickListener(tgClickListener);

    mDefaultTagGroup.setOnTagClick(mTagClickListener);
    mDefaultTagGroup.setOnTagClick(mTagClickListener);
    mSmallTagGroup.setOnTagClick(mTagClickListener);
    mLargeTagGroup.setOnTagClick(mTagClickListener);
    mBeautyTagGroup.setOnTagClick(mTagClickListener);
    mBeautyInverseTagGroup.setOnTagClick(mTagClickListener);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    String[] tags = mTagsManager.getTags();
    mPromptText.setVisibility((tags == null || tags.length == 0) ? View.VISIBLE : View.GONE);
    mDefaultTagGroup.setTags(tags);
    mSmallTagGroup.setTags(tags);
    mLargeTagGroup.setTags(tags);
    mBeautyTagGroup.setTags(tags);
    mBeautyInverseTagGroup.setTags(tags);
    tagSingleChoose.setTags(tags);
    tagSingleChoose.setChecked(0);
    tagMultiChoose.setTags(tags);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.menu_main_activity, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if (item.getItemId() == R.id.action_edit) {
      launchTagEditorActivity();
      return true;
    }
    return false;
  }

  protected void launchTagEditorActivity()
  {
    Intent intent = new Intent(MainActivity.this, TagEditorActivity.class);
    startActivity(intent);
  }

  class MyTagGroupOnClickListener implements View.OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      launchTagEditorActivity();
    }
  }
}