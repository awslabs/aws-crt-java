package com.amazonaws.s3;

import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class NotificationConfiguration {
  private List<TopicConfiguration> topicConfigurations;

  private List<QueueConfiguration> queueConfigurations;

  private List<LambdaFunctionConfiguration> lambdaFunctionConfigurations;

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
