package com.example.movies.ui.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.movies.R;
import com.example.movies.adapter.CelebritiesAdapter;
import com.example.movies.adapter.CommentAdapter;
import com.example.movies.adapter.Data2Adapter;
import com.example.movies.adapter.PhotosAdapter;
import com.example.movies.bean.Comments;
import com.example.movies.bean.Results;
import com.example.movies.dialog.CommentDialog;
import com.example.movies.enums.TypeEnum;
import com.example.movies.utils.HttpTool;
import com.example.movies.utils.OkHttpTool;
import com.example.movies.utils.RecyclerViewSpaces;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 电影明细
 */
public class MoviesDetailActivity extends AppCompatActivity {
    private Activity myActivity;
    private ImageView ivGo;
    private ImageView ivImg;
    private TextView tvRating;
    private TextView tvTitle;
    private TextView tvDirector;
    private TextView tvProtagonist;
    private TextView tvInfo;
    private TextView tvSummary;
    private LinearLayout llCollect;
    private ImageView ivCollect;
    private ImageView ivCollectCheck;
    private TextView tvCollect;
    private RatingBar ratingBar;
    private LinearLayout llComment;
    private RecyclerView rvActorList;
    private RecyclerView rvStillList;
    private RecyclerView rvCommentList;
    private RecyclerView rvPreferenceList;
    private CelebritiesAdapter celebritiesAdapter;
    private PhotosAdapter photosAdapter;
    private CommentAdapter commentAdapter;
    private Data2Adapter data2Adapter;
    private Results results;
    private boolean favorited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity = this;
        setContentView(R.layout.activity_movies_detail);
        ivGo = findViewById(R.id.iv_go);
        ivImg = findViewById(R.id.iv_photo);
        tvRating = findViewById(R.id.tv_rating);
        tvTitle = findViewById(R.id.title);
        tvDirector = findViewById(R.id.director);
        tvProtagonist = findViewById(R.id.protagonist);
        tvInfo = findViewById(R.id.info);
        tvSummary = findViewById(R.id.summary);
        llCollect = findViewById(R.id.ll_collect);
        ivCollect = findViewById(R.id.iv_collect);
        ivCollectCheck = findViewById(R.id.iv_collect_check);
        tvCollect = findViewById(R.id.tv_collect);
        ratingBar = findViewById(R.id.ratingBar);
        llComment = findViewById(R.id.ll_comment);
        rvActorList = findViewById(R.id.rv_actor_list);
        rvStillList = findViewById(R.id.rv_still_list);
        rvCommentList = findViewById(R.id.rv_comment_list);
        rvPreferenceList = findViewById(R.id.rv_preference_list);

        results = (Results) getIntent().getSerializableExtra("results");
        if (results != null) {
            //点击历史

            tvRating.setText(results.getRating());
            tvTitle.setText(results.getTitle());
            tvDirector.setText(String.format("导演 %s ", results.getCelebrities().get(0).getCelebrity().getName()));
            String protagonist = "";
            for (int i = 1; i < results.getCelebrities().size(); i++) {
                protagonist += results.getCelebrities().get(i).getCelebrity().getName() + " ";
            }
            tvProtagonist.setText(String.format("主演 %s ", protagonist));
            String genres = "";
            for (int i = 0; i < results.getGenres().size(); i++) {
                if (i == results.getGenres().size() - 1) {
                    genres = results.getGenres().get(i).getName();
                } else {
                    genres = results.getGenres().get(i).getName() + "/";
                }
            }
            tvInfo.setText(String.format("%s %s %s", results.getYear(), genres, results.getRuntime()));
            tvSummary.setText(String.format("%s", results.getSummary()));
            Glide.with(myActivity)
                    .asBitmap()
                    .load(results.getPoster_url())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(ivImg);
            //演员表
            LinearLayoutManager celebritiesLayoutManager = new LinearLayoutManager(myActivity);
            celebritiesLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rvActorList.setLayoutManager(celebritiesLayoutManager);
            celebritiesAdapter = new CelebritiesAdapter();
            rvActorList.setAdapter(celebritiesAdapter);
            celebritiesAdapter.addItem(results.getCelebrities());

            //剧照
            LinearLayoutManager stillLayoutManager = new LinearLayoutManager(myActivity);
            stillLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rvStillList.setLayoutManager(stillLayoutManager);
            photosAdapter = new PhotosAdapter();
            rvStillList.setAdapter(photosAdapter);
            photosAdapter.addItem(results.getPhotos());

            //评论
            LinearLayoutManager commentLayoutManager = new LinearLayoutManager(myActivity);
            commentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rvCommentList.setLayoutManager(commentLayoutManager);
            commentAdapter = new CommentAdapter();
            rvCommentList.setAdapter(commentAdapter);
            commentAdapter.addItem(results.getComments());

            //相似电影推荐
            GridLayoutManager layoutManager = new GridLayoutManager(myActivity, 5);//两列
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            //=1.3、设置recyclerView的布局管理器
            rvPreferenceList.setLayoutManager(layoutManager);
            HashMap<String, Integer> mapSpaces = new HashMap<>();//间距
            mapSpaces.put(RecyclerViewSpaces.TOP_DECORATION, 20);//上间距
            mapSpaces.put(RecyclerViewSpaces.BOTTOM_DECORATION, 20);//下间距
            mapSpaces.put(RecyclerViewSpaces.LEFT_DECORATION, 20);//左间距
            mapSpaces.put(RecyclerViewSpaces.RIGHT_DECORATION, 20);//右间距
            rvPreferenceList.addItemDecoration(new RecyclerViewSpaces(mapSpaces));//设置间距
            data2Adapter = new Data2Adapter(TypeEnum.Movies.getCode());
            rvPreferenceList.setAdapter(data2Adapter);
            data2Adapter.setItemListener(new Data2Adapter.ItemListener() {
                @Override
                public void ItemClick(Results results, int type) {
                    HttpTool.goDetail(myActivity,type,results.getId());
                }
            });
            isCollect();
            loadPreferenceList();

        }

        llCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favorited){//已收藏
                    collectCancel();
                }else {//未收藏
                    collectCheck();
                }
            }
        });

        llComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentDialog commentDialog =new CommentDialog(myActivity, new CommentDialog.OnItemClickListener() {
                    @Override
                    public void onSave(Dialog dialog, String content, String rating) {
                        String url = OkHttpTool.URL + "/movies/comments/";
                        String jsonStr = "{\"movie_id\":\""+results.getId()+"\",\"rating\":\""+rating+"\",\"content\":\""+content+"\"}";
                        OkHttpTool.httpPostJson(url, jsonStr, new OkHttpTool.ResponseCallback() {
                            @Override
                            public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {
                                myActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isSuccess && responseCode == 200) {
                                            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                                            Comments comments = gson.fromJson(response, Comments.class);
                                            results.getComments().add(comments);
                                            commentAdapter.addItem(results.getComments());
                                            Toast.makeText(myActivity, "发布成功", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
                commentDialog.show(getSupportFragmentManager(),"");
            }
        });

        ivGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(myActivity, PreferenceListActivity.class);
                intent.putExtra("type", TypeEnum.Movies.getCode());
                String url = OkHttpTool.URL + "/movies/"+results.getId()+"/recommendations/";
                intent.putExtra("url",url);
                startActivity(intent);
            }
        });


    }

    //判断是否已收藏
    private void isCollect(){
        String url = OkHttpTool.URL + "/user/movies/"+results.getId()+"/favorite/";
        Map<String, Object> map = new HashMap<>();
        OkHttpTool.httpGet(url, map, new OkHttpTool.ResponseCallback() {
            @Override
            public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isSuccess && responseCode == 200) {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response);
                                favorited = jsonObject.getBoolean("favorited");
                                ivCollect.setVisibility(favorited ?View.GONE:View.VISIBLE);
                                ivCollectCheck.setVisibility(favorited ?View.VISIBLE:View.GONE);
                                tvCollect.setText(favorited?"已收藏":"收藏");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }


    //收藏
    private void collectCheck(){
        String url = OkHttpTool.URL + "/user/movies/favorite/";
        String jsonStr = "{\"movie_id\":\""+results.getId()+"\"}";
        OkHttpTool.httpPostJson(url, jsonStr, new OkHttpTool.ResponseCallback() {
            @Override
            public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isSuccess && responseCode == 201) {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response);
                                String message = jsonObject.getString("message");
                                ivCollect.setVisibility(View.GONE);
                                ivCollectCheck.setVisibility(View.VISIBLE);
                                tvCollect.setText("已收藏");
                                favorited =true;
                                Toast.makeText(myActivity, "收藏成功", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

    //取消收藏
    private void collectCancel(){
        String url = OkHttpTool.URL + "/user/movies/favorite/";
        String jsonStr = "{\"movie_id\":\""+results.getId()+"\"}";
        OkHttpTool.httpDeleteJson(url, jsonStr, new OkHttpTool.ResponseCallback() {
            @Override
            public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (responseCode == -1) {
                            ivCollect.setVisibility(View.VISIBLE);
                            ivCollectCheck.setVisibility(View.GONE);
                            tvCollect.setText("收藏");
                            favorited =false;
                            Toast.makeText(myActivity, "取消收藏", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


    //获取相似推荐
    private void loadPreferenceList(){
        String url = OkHttpTool.URL + "/movies/"+results.getId()+"/recommendations/";
        Map<String, Object> map = new HashMap<>();
        OkHttpTool.httpGet(url, map, new OkHttpTool.ResponseCallback() {
            @Override
            public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isSuccess && responseCode == 200) {
                            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                            Type type = new TypeToken<List<Results>>() {
                            }.getType();//列表信息
                            List<Results> list = gson.fromJson(response, type);
                            data2Adapter.addItem(list);
                        }
                    }
                });
            }
        });
    }

    public void back(View view) {
        finish();
    }
}