package com.fanfan.novel.model;

import com.fanfan.novel.common.ChatConst;
import com.fanfan.novel.ui.recyclerview.tree.base.BaseItemData;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

/**
 * Created by android on 2017/12/20.
 */
@Entity
public class VoiceBean extends BaseItemData {

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "localType")
    @ChatConst.LocalType
    private int localType;
    @Property(nameInDb = "saveTime")
    private long saveTime;
    @Property(nameInDb = "showTitle")
    private String showTitle;
    @Property(nameInDb = "voiceanswer")
    private String voiceAnswer;
    @Property(nameInDb = "imgUrl")
    private String imgUrl;
    @Property(nameInDb = "action")
    private String action;
    @Property(nameInDb = "expression")
    private String expression;
    @Property(nameInDb = "actionData")
    private String actionData;
    @Property(nameInDb = "expressionData")
    private String expressionData;

    @Generated(hash = 8230440)
    public VoiceBean(Long id, int localType, long saveTime, String showTitle,
                     String voiceAnswer, String imgUrl, String action, String expression,
                     String actionData, String expressionData) {
        this.id = id;
        this.localType = localType;
        this.saveTime = saveTime;
        this.showTitle = showTitle;
        this.voiceAnswer = voiceAnswer;
        this.imgUrl = imgUrl;
        this.action = action;
        this.expression = expression;
        this.actionData = actionData;
        this.expressionData = expressionData;
    }

    @Generated(hash = 1719036352)
    public VoiceBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLocalType() {
        return this.localType;
    }

    public void setLocalType(int localType) {
        this.localType = localType;
    }

    public long getSaveTime() {
        return this.saveTime;
    }

    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }

    public String getShowTitle() {
        return this.showTitle;
    }

    public void setShowTitle(String showTitle) {
        this.showTitle = showTitle;
    }

    public String getVoiceAnswer() {
        return this.voiceAnswer;
    }

    public void setVoiceAnswer(String voiceAnswer) {
        this.voiceAnswer = voiceAnswer;
    }

    public String getImgUrl() {
        return this.imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getActionData() {
        return this.actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }

    public String getExpressionData() {
        return this.expressionData;
    }

    public void setExpressionData(String expressionData) {
        this.expressionData = expressionData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VoiceBean) {
            VoiceBean voiceBean= (VoiceBean) obj;
            return voiceBean.showTitle.equals(getShowTitle());
        }
        return false;
    }
}
