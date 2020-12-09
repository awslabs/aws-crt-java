package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class NotificationConfiguration {
  private List<TopicConfiguration> topicConfigurations;

  private List<QueueConfiguration> queueConfigurations;

  private List<LambdaFunctionConfiguration> lambdaFunctionConfigurations;

  public NotificationConfiguration() {
    this.topicConfigurations = null;
    this.queueConfigurations = null;
    this.lambdaFunctionConfigurations = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(NotificationConfiguration.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof NotificationConfiguration);
  }

  public List<TopicConfiguration> getTopicConfigurations() {
    return topicConfigurations;
  }

  public void setTopicConfigurations(final List<TopicConfiguration> topicConfigurations) {
    this.topicConfigurations = topicConfigurations;
  }

  public List<QueueConfiguration> getQueueConfigurations() {
    return queueConfigurations;
  }

  public void setQueueConfigurations(final List<QueueConfiguration> queueConfigurations) {
    this.queueConfigurations = queueConfigurations;
  }

  public List<LambdaFunctionConfiguration> getLambdaFunctionConfigurations() {
    return lambdaFunctionConfigurations;
  }

  public void setLambdaFunctionConfigurations(
      final List<LambdaFunctionConfiguration> lambdaFunctionConfigurations) {
    this.lambdaFunctionConfigurations = lambdaFunctionConfigurations;
  }
}
