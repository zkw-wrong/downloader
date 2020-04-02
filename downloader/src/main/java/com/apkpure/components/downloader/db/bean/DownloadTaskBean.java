package com.apkpure.components.downloader.db.bean;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import com.apkpure.components.downloader.db.Extras;
import com.apkpure.components.downloader.db.convert.ExtrasConvert;
import com.apkpure.components.downloader.db.convert.TaskStatusConverter;
import com.apkpure.components.downloader.db.enums.DownloadTaskStatusType;
import java.util.Date;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 下载任务
 *
 * @author xiongke
 * @date 2018/11/5
 */
@Entity(nameInDb = "download_tasks")
public class DownloadTaskBean implements Parcelable {

    @Id
    @NotNull
    @Property(nameInDb = "_download_url")
    @Index(unique = true)
    private String url;

    @Property(nameInDb = "_absolute_path")
    private String absolutePath;

    @Convert(converter = TaskStatusConverter.class, columnType = Integer.class)
    @Property(nameInDb = "_download_task_status_type")
    private DownloadTaskStatusType downloadTaskStatusType = DownloadTaskStatusType.Waiting;

    @Convert(converter = ExtrasConvert.class, columnType = String.class)
    @Property(nameInDb = "_extras")
    private Extras extras;

    @Property(nameInDb = "_date")
    private Date date = new Date();

    @Property(nameInDb = "_current_offset")
    private long currentOffset;

    @Property(nameInDb = "_total_length")
    private long totalLength;

    @Property(nameInDb = "_show_notification")
    private boolean showNotification;

    @Property(nameInDb = "_notification_id")
    private int notificationId;

    @Property(nameInDb = "_notification_title")
    private String notificationTitle;

    @Transient
    private String taskSpeed;

    protected DownloadTaskBean(Parcel in) {
        url = in.readString();
        absolutePath = in.readString();
        extras = in.readParcelable(Extras.class.getClassLoader());
        currentOffset = in.readLong();
        totalLength = in.readLong();
        showNotification = in.readByte() != 0;
        notificationId = in.readInt();
        notificationTitle = in.readString();
        taskSpeed = in.readString();
    }

    @Generated(hash = 684668229)
    public DownloadTaskBean(@NotNull String url, String absolutePath,
            DownloadTaskStatusType downloadTaskStatusType, Extras extras, Date date,
            long currentOffset, long totalLength, boolean showNotification,
            int notificationId, String notificationTitle) {
        this.url = url;
        this.absolutePath = absolutePath;
        this.downloadTaskStatusType = downloadTaskStatusType;
        this.extras = extras;
        this.date = date;
        this.currentOffset = currentOffset;
        this.totalLength = totalLength;
        this.showNotification = showNotification;
        this.notificationId = notificationId;
        this.notificationTitle = notificationTitle;
    }

    @Generated(hash = 2123101309)
    public DownloadTaskBean() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(absolutePath);
        dest.writeParcelable(extras, flags);
        dest.writeLong(currentOffset);
        dest.writeLong(totalLength);
        dest.writeByte((byte) (showNotification ? 1 : 0));
        dest.writeInt(notificationId);
        dest.writeString(notificationTitle);
        dest.writeString(taskSpeed);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DownloadTaskBean> CREATOR = new Creator<DownloadTaskBean>() {
        @Override
        public DownloadTaskBean createFromParcel(Parcel in) {
            return new DownloadTaskBean(in);
        }

        @Override
        public DownloadTaskBean[] newArray(int size) {
            return new DownloadTaskBean[size];
        }
    };

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

    public void setAbsolutePath(@NotNull String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public DownloadTaskStatusType getDownloadTaskStatusType() {
        return this.downloadTaskStatusType;
    }

    public void setDownloadTaskStatusType(DownloadTaskStatusType downloadTaskStatusType) {
        this.downloadTaskStatusType = downloadTaskStatusType;
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

    public String getNotificationTitle() {
        return this.notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    @Nullable
    public Extras getExtras() {
        return this.extras;
    }

    public void setExtras(@Nullable Extras extras) {
        this.extras = extras;
    }
}
