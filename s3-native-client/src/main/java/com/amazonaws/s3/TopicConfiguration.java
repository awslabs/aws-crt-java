package com.amazonaws.s3;

import java.lang.String;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class TopicConfiguration {
  private String id;

  private String topicArn;

  private List<Event> events;

  private NotificationConfigurationFilter filter;

  /**
   * <p>An optional unique identifier for configurations in a notification configuration. If you
   *          don't provide one, Amazon S3 will assign an ID.</p>
   */
  public String getId() {
    return id;
  }

  /**
   * <p>An optional unique identifier for configurations in a notification configuration. If you
   *          don't provide one, Amazon S3 will assign an ID.</p>
   */
  public void setId(final String id) {
    this.id = id;
  }

  public String getTopicArn() {
    return topicArn;
  }

  public void setTopicArn(final String topicArn) {
    this.topicArn = topicArn;
  }

  public List<Event> getEvents() {
    return events;
  }

  public void setEvents(final List<Event> events) {
    this.events = events;
  }

  /**
   * <p>Specifies object key name filtering rules. For information about key name filtering, see
   *             <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html">Configuring
   *             Event Notifications</a> in the <i>Amazon Simple Storage Service Developer
   *          Guide</i>.</p>
   */
  public NotificationConfigurationFilter getFilter() {
    return filter;
  }

  /**
   * <p>Specifies object key name filtering rules. For information about key name filtering, see
   *             <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html">Configuring
   *             Event Notifications</a> in the <i>Amazon Simple Storage Service Developer
   *          Guide</i>.</p>
   */
  public void setFilter(final NotificationConfigurationFilter filter) {
    this.filter = filter;
  }
}
