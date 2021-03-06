package com.ozner.cup.mycenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ozner.cup.BaiduPush.OznerBroadcastAction;
import com.ozner.cup.Command.CenterUrlContants;
import com.ozner.cup.Command.Contants;
import com.ozner.cup.Command.FootFragmentListener;
import com.ozner.cup.Command.ImageHelper;
import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.NetUserHeadImg;
import com.ozner.cup.HttpHelper.NetUserVfMessage;
import com.ozner.cup.HttpHelper.OznerDataHttp;
import com.ozner.cup.Login.LoginActivity;
import com.ozner.cup.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.mycenter.CenterBean.CenterNotification;
import com.ozner.cup.mycenter.CenterBean.CenterVipUtil;
import com.ozner.cup.slideleft.BaseFragment;
import com.ozner.device.OznerDeviceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by gong on 2015/11/27.
 */
public class MyFragment extends BaseFragment implements View.OnClickListener, FootFragmentListener {
    private static final String TAG = "MyFragment";
    private final int USER_HEAD_INFO = 1;//
    private final int ADVISE_REQUEST = 2;
    private final int VERFIY_MSG = 3;//验证信息
    RelativeLayout rlay_center_shared, rlay_invite_vip, rlay_win_prize, rlay_myFriend, rlay_viewReport;
    RelativeLayout setting_layout, rlay_advise;
    LinearLayout llay_myDevice, llay_myMoney;
    ImageView iv_person_photo;
    TextView tv_name, tv_myScore, tv_mydeviceNum, tv_gradeNmae, tv_newFriendNum;
    MyCenterHandle uihandle = new MyCenterHandle();
    ImageHelper imageHelper ;
    int deviceNum = 0;
    MyLoadImgListener imageLoadListener = new MyLoadImgListener();
    String userid, mobile, usertoken;
    byte centerNotify = 0;

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        Log.e(TAG, "onDestroyView: ");
        System.gc();
        super.onDestroyView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(getLayoutId(), container, false);
        ((ImageView) rootView.findViewById(R.id.iv_person_photo)).setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.mipmap.icon_default_headimage));
        ((ImageView) rootView.findViewById(R.id.iv_center_share)).setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.center_myorder));
        ((ImageView) rootView.findViewById(R.id.iv_myredbag)).setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.center_redbag));
        ((ImageView) rootView.findViewById(R.id.iv_myticket)).setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.center_myticket));

        ((RelativeLayout) rootView.findViewById(R.id.rlay_person_photo_bg)).setBackground(ImageHelper.loadResDrawable(getContext(), R.drawable.center_person_bg));
        return rootView;
    }

    private void initView(View view) {
        OznerApplication.changeTextFont((ViewGroup) view);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getActivity().getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.fz_blue));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.fz_blue));
        }

        llay_myDevice = (LinearLayout) view.findViewById(R.id.llay_myDevice);
        llay_myMoney = (LinearLayout) view.findViewById(R.id.llay_myMoney);
        rlay_center_shared = (RelativeLayout) view.findViewById(R.id.rlay_center_shared);
        rlay_invite_vip = (RelativeLayout) view.findViewById(R.id.rlay_invite_vip);
        rlay_win_prize = (RelativeLayout) view.findViewById(R.id.rlay_win_prize);
        rlay_myFriend = (RelativeLayout) view.findViewById(R.id.person_center_modify_password);
        setting_layout = (RelativeLayout) view.findViewById(R.id.setting_layout);
        rlay_advise = (RelativeLayout) view.findViewById(R.id.private_message_layout);
        iv_person_photo = (ImageView) view.findViewById(R.id.iv_person_photo);
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_mydeviceNum = (TextView) view.findViewById(R.id.tv_mydeviceNum);
        tv_gradeNmae = (TextView) view.findViewById(R.id.tv_gradeNmae);
        tv_myScore = (TextView) view.findViewById(R.id.tv_myScore);
        rlay_viewReport = (RelativeLayout) view.findViewById(R.id.person_center_binding_setting);
        tv_newFriendNum = (TextView) view.findViewById(R.id.tv_newFriendNum);
        rlay_center_shared.setOnClickListener(this);
        rlay_invite_vip.setOnClickListener(this);
        rlay_win_prize.setOnClickListener(this);
        rlay_myFriend.setOnClickListener(this);
        rlay_advise.setOnClickListener(this);
        rlay_viewReport.setOnClickListener(this);
        setting_layout.setOnClickListener(this);
        llay_myDevice.setOnClickListener(this);
        iv_person_photo.setOnClickListener(this);
        llay_myMoney.setOnClickListener(this);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(getView());
        userid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, null);
        mobile = UserDataPreference.GetUserData(getContext(), UserDataPreference.Mobile, null);
        usertoken = OznerPreference.UserToken(getActivity());
        Log.i("tag", "usertoken:" + usertoken);
        imageHelper = new ImageHelper(getContext());
        imageHelper.setImageLoadingListener(imageLoadListener);
    }

    private void initCenterState() {
        centerNotify = CenterNotification.getCenterNotifyState(getContext());
//        Log.e("tag", "centerNotify:" + centerNotify);
        if (centerNotify > 0) {
            tv_newFriendNum.setVisibility(View.VISIBLE);
            ((MainActivity) getActivity()).footNavFragment.SetCenterNotify(centerNotify);
        } else {
            tv_newFriendNum.setVisibility(View.GONE);
            ((MainActivity) getActivity()).footNavFragment.SetCenterNotify(centerNotify);
        }
    }

    @Override
    public void onResume() {
        if (userid != null && userid.length() > 0) {
            initCenterState();
            initHeadImg();
            initMyDevice();
            initVerifyMsg();
        }
        super.onResume();
    }

    private int getLayoutId() {
        return R.layout.fragment_mycenter_layout;
    }

    //初始化个人信息
    private void initHeadImg() {
        final String url = OznerPreference.ServerAddress(getContext()) + "/OznerServer/GetUserNickImage";
        loadUserHeadImg(getActivity());
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetUserHeadImg netUserHeadImg = centerInitUserHeadImg(getActivity(), url);
                Message message = new Message();
                message.what = USER_HEAD_INFO;
                message.obj = netUserHeadImg;
                uihandle.sendMessage(message);
            }
        }).start();
    }

    private void initMyDevice() {
        try {
            deviceNum = OznerDeviceManager.Instance().getDevices().length;
            tv_mydeviceNum.setText(String.valueOf(deviceNum));
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "initMyDevice_Ex: "+ex.getMessage());
        }
    }

    private void initVerifyMsg() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<NetUserVfMessage> vfInfoList = OznerCommand.GetUserVerifMessage(getActivity());
                Message message = new Message();
                message.what = VERFIY_MSG;
                message.obj = vfInfoList;
                uihandle.sendMessage(message);
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_person_photo:
                if (userid == null || userid == "") {
                    Intent loginIntent = new Intent(getContext(), LoginActivity.class);
                    startActivity(loginIntent);
                    getActivity().finish();
                }
                break;
            case R.id.rlay_center_shared://我的订单
                Intent sharedIntent = new Intent(getContext(), WebActivity.class);
                String shardUrl = CenterUrlContants.formatMyOrderUrl(mobile, usertoken, "zh", "zh");
                Log.e("tag", "订单:" + shardUrl);
                sharedIntent.putExtra(WebActivity.URL, shardUrl);
                startActivity(sharedIntent);
                break;
            case R.id.rlay_invite_vip://领红包
                Intent inviteIntent = new Intent(getContext(), WebActivity.class);
                String redPacUrl = CenterUrlContants.formatRedPacUrl(mobile, usertoken, "zh", "zh");
                Log.e("tag", "领红包:" + redPacUrl);
                inviteIntent.putExtra(WebActivity.URL, redPacUrl);
                inviteIntent.putExtra("IsRedBag", true);
                inviteIntent.putExtra(WebActivity.TITLE, getString(R.string.Center_getRedbag));
                startActivity(inviteIntent);
                break;
            case R.id.rlay_win_prize://我的券
                Intent winIntent = new Intent(getContext(), WebActivity.class);
                String myTicketUrl = CenterUrlContants.formatMyTicketUrl(mobile, usertoken, "zh", "zh");
                winIntent.putExtra(WebActivity.URL, myTicketUrl);
                startActivity(winIntent);
                break;
            case R.id.person_center_modify_password://我的好友
                Intent friendsIntent = new Intent(getContext(), MyFriendsActivity.class);
                startActivity(friendsIntent);
                break;
            case R.id.private_message_layout://我要提意见
                Intent adviseIntent = new Intent(getContext(), AdviseActivity.class);
                startActivityForResult(adviseIntent, ADVISE_REQUEST);
                break;
            case R.id.setting_layout:
                Intent setupIntent = new Intent(getContext(), CenterSetupActivity.class);
                startActivity(setupIntent);
                break;
            case R.id.llay_myDevice://我的设备
                Intent deviceIntent = new Intent(getContext(), MyDeviceActivity.class);
                startActivityForResult(deviceIntent, 111);
                break;
            case R.id.llay_myMoney://我的小金库
                Intent moneyIntent = new Intent(getContext(), WebActivity.class);
                String moneyUrl = CenterUrlContants.formatMyMoneyUrl(mobile, usertoken, "zh", "zh");
                Log.e("tag", "小金库:" + moneyUrl);
                moneyIntent.putExtra(WebActivity.URL, moneyUrl);
                moneyIntent.putExtra(WebActivity.TITLE, getString(R.string.Center_MyCoffers));
                startActivity(moneyIntent);
                break;
            case R.id.person_center_binding_setting://水质检测报告
                Intent reportIntent = new Intent();
                String reportUrl = String.format(Contants.Water_Analysis, UserDataPreference.GetUserData(getContext(), UserDataPreference.Mobile, ""));
                reportIntent.putExtra(WebActivity.URL, reportUrl);
                reportIntent.setClass(getContext(), WebActivity.class);
                startActivity(reportIntent);
                break;
        }
    }


    class MyCenterHandle extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            if (MyFragment.this.isAdded()) {
                switch (msg.what) {
                    case USER_HEAD_INFO:
                        NetUserHeadImg netUserHeadImg = (NetUserHeadImg) msg.obj;
                        if (netUserHeadImg != null) {

                            tv_name.setText((netUserHeadImg.nickname != null && netUserHeadImg.nickname.length() > 0) ? netUserHeadImg.nickname : netUserHeadImg.mobile);
                            tv_myScore.setText(String.valueOf(netUserHeadImg.Score));
                            if (netUserHeadImg.headimg != null && netUserHeadImg.headimg.length() > 0) {
                                imageHelper.loadImage(iv_person_photo, netUserHeadImg.headimg);
                            } else {
                                //imageHelper.loadImage(iv_person_photo, "http://a.hiphotos.baidu.com/zhidao/wh%3D600%2C800/sign=10284cd567380cd7e64baaeb9174810c/63d9f2d3572c11df09ba0c46612762d0f703c268.jpg");
//                                iv_person_photo.setImageResource(R.mipmap.icon_default_headimage);
                                iv_person_photo.setImageBitmap(ImageHelper.loadResBitmap(getContext(),R.mipmap.icon_default_headimage));
                            }
                            if (netUserHeadImg.gradename != null && netUserHeadImg.gradename != "") {
                                String gradename = netUserHeadImg.gradename;
                                if (gradename.contains("会员")) {
                                    int index = gradename.indexOf("会员");
                                    gradename = gradename.substring(0, index);
                                }
                                if (MyFragment.this.isAdded() &&
                                        !MyFragment.this.isRemoving() &&
                                        !MyFragment.this.isDetached()) {
                                    if (!((OznerApplication) getActivity().getApplication()).isLanguageCN()) {
                                        if (CenterVipUtil.hasValue(gradename)) {
                                            gradename = CenterVipUtil.getEnValue(gradename);
                                        }
                                    }
                                }
                                gradename += getString(R.string.act_member);
                                tv_gradeNmae.setText(gradename);
                                tv_gradeNmae.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    case VERFIY_MSG:
                        List<NetUserVfMessage> vfMsglist  = (List<NetUserVfMessage>) msg.obj;
                        int waitNum = 0;
                        for (NetUserVfMessage vfmsg : vfMsglist) {
                            if (vfmsg.Status != 2) {
                                waitNum++;
                            }
                        }
                        if (waitNum > 0) {
//                            tv_newFriendNum.setText(waitNum + "");
                            //设置新的验证信息标志
//                            tv_newFriendNum.setVisibility(View.VISIBLE);
                            CenterNotification.setCenterNotify(getContext(), CenterNotification.NewFriendVF);
                            initCenterState();
                        } else {
//                            tv_newFriendNum.setText("");
                            //重置新的验证信息
//                            tv_newFriendNum.setVisibility(View.GONE);
                            CenterNotification.resetCenterNotify(getContext(), CenterNotification.DealNewFriendVF);
                            initCenterState();
                        }
                        break;

                }
                super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ADVISE_REQUEST && getActivity().RESULT_OK == resultCode) {
            Toast toast = new Toast(getContext());
            View layout = LayoutInflater.from(getContext()).inflate(R.layout.advise_result_toast, null);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setView(layout);
            toast.show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadUserHeadImg(final Activity activity) {
        NetUserHeadImg netUserHeadImg = new NetUserHeadImg();
        netUserHeadImg.fromPreference(activity);
        if (netUserHeadImg != null) {
            Message message = new Message();
            message.what = USER_HEAD_INFO;
            message.obj = netUserHeadImg;
            uihandle.sendMessage(message);
        }
    }

    public static NetUserHeadImg centerInitUserHeadImg(final Activity activity, final String inituserHeadUrl) {
        NetUserHeadImg netUserHeadImg = new NetUserHeadImg();
        String Mobile = UserDataPreference.GetUserData(activity, UserDataPreference.Mobile, null);
        if (Mobile != null) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(activity)));
            params.add(new BasicNameValuePair("jsonmobile", Mobile));
            NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, inituserHeadUrl, params);
            if (netJsonObject.state > 0) {
                try {
                    JSONArray jarry = netJsonObject.getJSONObject().getJSONArray("data");
                    if (jarry.length() > 0) {
                        JSONObject jo = (JSONObject) jarry.get(0);
                        netUserHeadImg.fromJSONobject(jo);
                        UserDataPreference.SaveUserData(activity, jo);
                    } else {
                        netUserHeadImg.fromPreference(activity);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    netUserHeadImg.fromPreference(activity);
                }
            }
        }
        netUserHeadImg.fromPreference(activity);
        return netUserHeadImg;
    }


    class MyLoadImgListener extends SimpleImageLoadingListener {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ((ImageView) view).setImageBitmap(ImageHelper.toRoundBitmap(getContext(), loadedImage));
            super.onLoadingComplete(imageUri, view, loadedImage);
        }
    }

    @Override
    public void ShowContent(int i, String mac) {

    }

    @Override
    public void ChangeRawRecord() {

    }

    @Override
    public void CupSensorChange(String address) {

    }

    @Override
    public void DeviceDataChange() {

    }

    @Override
    public void ContentChange(String mac, String state) {

    }

    @Override
    public void RecvChatData(String data) {
        switch (data) {
            case OznerBroadcastAction.NewMessage:
            case OznerBroadcastAction.NewRank:
            case OznerBroadcastAction.NewFriendVF:
            case OznerBroadcastAction.NewFriend:
                initCenterState();
                break;
        }
    }
}
