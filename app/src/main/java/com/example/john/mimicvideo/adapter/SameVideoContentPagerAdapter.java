package com.example.john.mimicvideo.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.john.mimicvideo.CommentActivity;
import com.example.john.mimicvideo.LoginActivity;
import com.example.john.mimicvideo.OtherProfileActivity;
import com.example.john.mimicvideo.R;
import com.example.john.mimicvideo.ShareActivity;
import com.example.john.mimicvideo.VideoSampleActivity;
import com.example.john.mimicvideo.model.VideoContent;
import com.example.john.mimicvideo.utils.ApplicationService;
import com.example.john.mimicvideo.utils.JSONParser;
import com.example.john.mimicvideo.utils.SharePreferenceDB;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2018/3/24.
 */

public class SameVideoContentPagerAdapter extends PagerAdapter{
    private String TAG = SameVideoContentPagerAdapter.class.getSimpleName();
    Context context;
    List<VideoContent>sameVideoContentList;
    private VideoView videoContentVideoView;
    private ImageView ownerProfileImg;
    private TextView ownerNameTxt;
    private ImageView openCommentImg;
    private ImageView openVideoSampleImg;
    private ImageView openSameVideoContentImg;
    private ImageView giveLikeImg;
    private ImageView reportImg;
    private TextView shareTxt;
    private TextView videoContentTitleTxt;
    private LinearLayout ownerLayout;
    private TextView likeAmountTxt;
    private TextView commentAmountTxt;
    private JSONParser jsonParser = new JSONParser();
    SharePreferenceDB sharePreferenceDB;
    private int user_id;

    public SameVideoContentPagerAdapter(Context context, List<VideoContent> sameVideoContentList){
        this.context = context;
        this.sameVideoContentList = sameVideoContentList;
        sharePreferenceDB = new SharePreferenceDB(context);
        user_id = sharePreferenceDB.getInt("id");
    }

    @Override
    public int getCount() {
        return sameVideoContentList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return o == view;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        final VideoContent videoContent = sameVideoContentList.get(position);

        if(position == 6){
            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.pager_row_video_sample, null);
            container.addView(linearLayout);
            videoContentVideoView = linearLayout.findViewById(R.id.videoContentVideoView);
            openVideoSampleImg = linearLayout.findViewById(R.id.openVideoSampleImg);
            reportImg = linearLayout.findViewById(R.id.reportImg);
            shareTxt = linearLayout.findViewById(R.id.shareTxt);
            videoContentTitleTxt = linearLayout.findViewById(R.id.videoContentTitleTxt);

            openVideoSampleImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(context, VideoSampleActivity.class);
                    ((Activity)context).startActivity(intent);
                }
            });

            reportImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new ReportSubmit("").execute();
                }
            });

            shareTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            videoContentTitleTxt.setText(videoContent.title);

            return linearLayout;
        }else{
            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.pager_row_video_content, null);
            container.addView(linearLayout);
            videoContentVideoView = linearLayout.findViewById(R.id.videoContentVideoView);
            ownerProfileImg = linearLayout.findViewById(R.id.ownerProfileImg);
            ownerNameTxt = linearLayout.findViewById(R.id.ownerNameTxt);
            openCommentImg = linearLayout.findViewById(R.id.openCommentImg);
            openVideoSampleImg = linearLayout.findViewById(R.id.openVideoSampleImg);
            openSameVideoContentImg = linearLayout.findViewById(R.id.openSameVideoContentImg);
            openSameVideoContentImg.setVisibility(View.GONE);
            giveLikeImg = linearLayout.findViewById(R.id.giveLikeImg);
//            reportImg = linearLayout.findViewById(R.id.reportImg);
            shareTxt = linearLayout.findViewById(R.id.shareTxt);
            videoContentTitleTxt = linearLayout.findViewById(R.id.videoContentTitleTxt);
            ownerLayout = linearLayout.findViewById(R.id.ownerLayout);
            likeAmountTxt = linearLayout.findViewById(R.id.likeAmountTxt);
            commentAmountTxt = linearLayout.findViewById(R.id.commentAmountTxt);

            likeAmountTxt.setText(String.valueOf(videoContent.likeAmount));
            commentAmountTxt.setText(String.valueOf(videoContent.commentAmount));

            ownerLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(context, OtherProfileActivity.class);
                    intent.putExtra("id", videoContent.owner.id);
                    intent.putExtra("name", videoContent.owner.name);
                    intent.putExtra("profile", videoContent.owner.profile);
                    context.startActivity(intent);
                }
            });

            shareTxt.setTypeface(ApplicationService.getFont());
            shareTxt.setText(R.string.fa_share_square_o);

            Glide.with(context)
                    .load(videoContent.owner.profile)
                    .into(ownerProfileImg);
            ownerNameTxt.setText(videoContent.owner.name);

            openCommentImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.putExtra("video_content_id", videoContent.id);
                    intent.setClass(context, CommentActivity.class);
                    ((Activity)context).startActivity(intent);
                }
            });

            openVideoSampleImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(context, VideoSampleActivity.class);
                    context.startActivity(intent);
                }
            });

            for(int i = 0; i < videoContent.likeList.size(); i++){
                if(videoContent.likeList.get(i).user_id == user_id){
                    if(videoContent.likeList.get(i).is_click == 1){
                        giveLikeImg.setBackgroundColor(context.getResources().getColor(R.color.com_facebook_button_background_color));
                    }else{
                        giveLikeImg.setBackgroundColor(context.getResources().getColor(R.color.white));
                    }
                }else{
                    giveLikeImg.setBackgroundColor(context.getResources().getColor(R.color.white));
                }
            }

            giveLikeImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(sharePreferenceDB.getInt("id") != 0){
                        if(((ColorDrawable)view.getBackground()).getColor() == context.getResources().getColor(R.color.com_facebook_button_background_color)){
                            view.setBackgroundColor(context.getResources().getColor(R.color.white));
                            new UpdateLikeAmount(user_id, videoContent.id, 0).execute();
                        }else{
                            view.setBackgroundColor(context.getResources().getColor(R.color.com_facebook_button_background_color));
                            new UpdateLikeAmount(user_id, videoContent.id, 1).execute();
                        }
                    }else{
                        Intent intent = new Intent();
                        intent.setClass(context, LoginActivity.class);
                        context.startActivity(intent);
                    }
                }
            });

//            reportImg.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    final Dialog dialog = new Dialog(context, R.style.selectorDialog);
//                    dialog.setContentView(R.layout.dialog_report);
//                    RecyclerView reportDescriptionRV = dialog.findViewById(R.id.reportDescriptionRV);
//                    Button reportSubmitBtn = dialog.findViewById(R.id.reportSubmitBtn);
//
//                    LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
//                    ReportDescriptionAdapter reportDescriptionAdapter = new ReportDescriptionAdapter(context);
//                    reportDescriptionRV.setLayoutManager(layoutManager);
//                    reportDescriptionRV.setAdapter(reportDescriptionAdapter);
//
//                    reportSubmitBtn.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            new ReportSubmit("").execute();
//                        }
//                    });
//
//
//                    // 由程式設定 Dialog 視窗外的明暗程度, 亮度從 0f 到 1f
//                    WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
//                    lp.dimAmount = 0.5f;
//                    dialog.getWindow().setAttributes(lp);
//                    dialog.show();
//                }
//            });

            shareTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(context, ShareActivity.class);
                    context.startActivity(intent);
                }
            });

            videoContentTitleTxt.setText(videoContent.title);


            return linearLayout;
        }
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);

    }

    //    public void shareVideoContent(){
//        LinearLayout facebook = dialog.findViewById(R.id.facebook);
//        LinearLayout messenger = dialog.findViewById(R.id.messenger);
//        LinearLayout line = dialog.findViewById(R.id.line);
//        LinearLayout copy = dialog.findViewById(R.id.copy);
//
//        FacebookCallback<Sharer.Result> facebook_callback = new FacebookCallback<Sharer.Result>(){
//            @Override
//            public void onSuccess(Sharer.Result result) {
//                Log.d(TAG, "成功分享至Facebook");
//            }
//
//            @Override
//            public void onCancel() {
//                Log.d(TAG, "取消分享至Facebook");
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                Log.e(TAG, error.getMessage());
//            }
//        };
//
//        FacebookCallback<Sharer.Result> messenger_callback = new FacebookCallback<Sharer.Result>(){
//            @Override
//            public void onSuccess(Sharer.Result result) {
//                Log.d(TAG, "成功分享至Messenger");
//            }
//
//            @Override
//            public void onCancel() {
//                Log.d(TAG, "取消分享至Messenger");
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                Log.e(TAG, "分享至Messenger發生錯誤" + " " + error.getMessage());
//            }
//        };
//
//        msgDialog = new MessageDialog(this);
//        msgDialog.registerCallback(callbackManager, messenger_callback, MSG_CODE);
//        shareDialog = new ShareDialog(this);
//        shareDialog.registerCallback(callbackManager, facebook_callback, FB_CODE);
//
//        facebook.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (ShareDialog.canShow(ShareLinkContent.class)) {
//                    try {
//                        ShareLinkContent content = new ShareLinkContent.Builder()
//                                .setContentUrl(Uri.parse("https://www." + getResources().getString(R.string.domain_title) + ".com.tw" + "?sid=o_screenshotshare&from=o_screenshotshare"))
//                                .setQuote("我看到好物想分享給你，快來" + getString(R.string.app_name)+ "App看看😍")
//                                .build();
//
//                        shareDialog.show(content);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    goToApp(getString(R.string.fb_package_name));
//                }
//            }
//        });
//
//        messenger.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (MessageDialog.canShow(ShareLinkContent.class)) {
//
//                    try {
////                                SharePhoto photo = new SharePhoto.Builder()
////                                        .setBitmap(icon)
////                                        .build();
////                        SharePhotoContent content = new SharePhotoContent.Builder()
////                                .addPhoto(photo)
////                                .setContentUrl(Uri.parse("https://www." + getResources().getString(R.string.domain_title) + ".com.tw"))
////                                .build();
//
//                        ShareLinkContent content = new ShareLinkContent.Builder()
//                                .setContentUrl(Uri.parse("https://www." + getResources().getString(R.string.domain_title) + ".com.tw" + "?sid=o_screenshotshare&from=o_screenshotshare"))
//                                .setQuote("我看到好物想分享給你，快來" + getString(R.string.app_name)+ "App看看😍")
//                                .build();
//
//                        msgDialog.show(content);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
//                    goToApp(getString(R.string.fb_messenger_package_name));
//                }
//            }
//        });
//
//        line.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (getPackageIntent(getString(R.string.line_package_name)) != null) {
//                    try {
//                        ApplicationService.pushEvent("screenshot share", "分享至Line", null, null);
//                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("line://msg/text/" + "我看到好物想分享給你，快來生活市集App看看😍" + "   https://www." + getResources().getString(R.string.domain_title) + ".com.tw" + "?sid=o_screenshotshare&from=o_screenshotshare")));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    goToApp(getString(R.string.line_package_name));
//                }
//            }
//        });
//    }


    //send the report description
    class ReportSubmit extends AsyncTask<String, String, String> {
        String reportDescription;

        ReportSubmit(String reportDescription) {
            this.reportDescription = reportDescription;
        }

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args) {
//            String name = inputName.getText().toString();
//            String price = inputPrice.getText().toString();
//            String description = inputDesc.getText().toString();
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("type", "0"));
            params.add(new BasicNameValuePair("reportDescription", reportDescription));


            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject jsonObject = jsonParser.makeHttpRequest("http://1.34.63.239/create_report.php",
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", jsonObject.toString());

//            // check for success tag
//            try {
//                int success = json.getInt(TAG_SUCCESS);
//
//                if (success == 1) {
//                    // successfully created product
//                    Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
//                    startActivity(i);
//
//                    // closing this screen
//                    finish();
//                } else {
//                    // failed to create product
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
        }

    }

    class UpdateLikeAmount extends AsyncTask<String, String, String> {
        int user_id;
        int video_content_id;
        int is_click;

        public UpdateLikeAmount(int user_id, int video_content_id, int is_click){
            this.user_id = user_id;
            this.video_content_id = video_content_id;
            this.is_click =is_click;
        }

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
            params.add(new BasicNameValuePair("video_content_id", String.valueOf(video_content_id)));
            params.add(new BasicNameValuePair("is_click", String.valueOf(is_click)));
            // getting JSON string from URL
            JSONObject json = jsonParser.makeHttpRequest("http://1.34.63.239/give_video_content_like.php", "POST", params);

            if(json != null){
                // Check your log cat for JSON reponse
                Log.d("Video data: ", json.toString());

                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt("success");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {

        }

    }

}
