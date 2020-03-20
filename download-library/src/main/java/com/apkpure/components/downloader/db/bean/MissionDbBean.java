package com.apkpure.components.downloader.db.bean;

import androidx.annotation.Nullable;

import com.apkpure.components.downloader.db.convert.MissionScoreTypeConverter;
import com.apkpure.components.downloader.db.enums.MissionScoreType;
import com.apkpure.components.downloader.db.enums.MissionStatusType;
import com.xk.gvido.app.model.db.convert.MissionStatusTypeConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Date;

/**
 * 下载任务
 *
 * @author xiongke
 * @date 2018/11/5
 */
@Entity(nameInDb = "mission")
public class MissionDbBean {

    @Id
    @NotNull
    @Property(nameInDb = "_download_url")
    @Index(unique = true)
    private String url;

    @Property(nameInDb = "_short_name")
    private String shortName;

    @Property(nameInDb = "_absolute_path")
    private String absolutePath;

    @Property(nameInDb = "_param_date")
    private String paramData;

    @Convert(converter = MissionScoreTypeConverter.class, columnType = Integer.class)
    @Property(nameInDb = "_mission_score_type")
    private MissionScoreType missionScoreType = MissionScoreType.UNKNOWN;

    @Convert(converter = MissionStatusTypeConverter.class, columnType = Integer.class)
    @Property(nameInDb = "_mission_status_type")
    private MissionStatusType missionStatusType = MissionStatusType.Waiting;

    @Property(nameInDb = "_date")
    private Date date = new Date();

    @Property(nameInDb = "_current_offset")
    private long currentOffset;

    @Property(nameInDb = "_total_length")
    private long totalLength;

    @Property(nameInDb = "_show_notification")
    private boolean showNotification;

    @Property(nameInDb = "_notification_id")
    private int notificationId = -1;

    @Transient
    private String taskSpeed;

    @Transient
    private String downloadPercent;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @NotNull
    public String getAbsolutePath() {
        return this.absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    @Nullable
    public String getParamData() {
        return this.paramData;
    }

    public void setParamData(String paramData) {
        this.paramData = paramData;
    }

    public MissionScoreType getMissionScoreType() {
        return this.missionScoreType;
    }

    public void setMissionScoreType(MissionScoreType missionScoreType) {
        this.missionScoreType = missionScoreType;
    }

    public MissionStatusType getMissionStatusType() {
        return this.missionStatusType;
    }

    public void setMissionStatusType(MissionStatusType missionStatusType) {
        this.missionStatusType = missionStatusType;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getCurrentOffset() {
        return this.currentOffset;
    }

    public void setCurrentOffset(long currentOffset) {
        this.currentOffset = currentOffset;
    }

    public long getTotalLength() {
        return this.totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    @Nullable
    public String getTaskSpeed() {
        return taskSpeed;
    }

    public void setTaskSpeed(String taskSpeed) {
        this.taskSpeed = taskSpeed;
    }

    @Nullable
    public String getShortName() {
        return this.shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public boolean getShowNotification() {
        return this.showNotification;
    }

    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }

    public int getNotificationId() {
        return this.notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getDownloadPercent() {
        return downloadPercent;
    }

    public void setDownloadPercent(String downloadPercent) {
        this.downloadPercent = downloadPercent;
    }
}
