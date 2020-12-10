package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.UnionGenerator")
public class SelectObjectContentEventStream {
  private RecordsEvent records;

  private StatsEvent stats;

  private ProgressEvent progress;

  private ContinuationEvent cont;

  private EndEvent end;

  private SelectObjectContentEventStream(Builder builder) {
    this.records = builder.records;
    this.stats = builder.stats;
    this.progress = builder.progress;
    this.cont = builder.cont;
    this.end = builder.end;
  }

  @Override
  public int hashCode() {
    return Objects.hash(SelectObjectContentEventStream.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof SelectObjectContentEventStream);
  }

  public RecordsEvent records() {
    return records;
  }

  public StatsEvent stats() {
    return stats;
  }

  public ProgressEvent progress() {
    return progress;
  }

  public ContinuationEvent cont() {
    return cont;
  }

  public EndEvent end() {
    return end;
  }

  public Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  static final class Builder {
    private RecordsEvent records;

    private StatsEvent stats;

    private ProgressEvent progress;

    private ContinuationEvent cont;

    private EndEvent end;

    private Builder() {
    }

    private Builder(SelectObjectContentEventStream model) {
      records(model.records);
      stats(model.stats);
      progress(model.progress);
      cont(model.cont);
      end(model.end);
    }

    public SelectObjectContentEventStream build() {
      return new com.amazonaws.s3.SelectObjectContentEventStream(this);
    }

    public RecordsEvent getRecords() {
      return records;
    }

    public void setRecords(final RecordsEvent records) {
      this.records = records;
    }

    public final Builder records(RecordsEvent records) {
      this.records = records;
      return this;
    }

    public StatsEvent getStats() {
      return stats;
    }

    public void setStats(final StatsEvent stats) {
      this.stats = stats;
    }

    public final Builder stats(StatsEvent stats) {
      this.stats = stats;
      return this;
    }

    public ProgressEvent getProgress() {
      return progress;
    }

    public void setProgress(final ProgressEvent progress) {
      this.progress = progress;
    }

    public final Builder progress(ProgressEvent progress) {
      this.progress = progress;
      return this;
    }

    public ContinuationEvent getCont() {
      return cont;
    }

    public void setCont(final ContinuationEvent cont) {
      this.cont = cont;
    }

    public final Builder cont(ContinuationEvent cont) {
      this.cont = cont;
      return this;
    }

    public EndEvent getEnd() {
      return end;
    }

    public void setEnd(final EndEvent end) {
      this.end = end;
    }

    public final Builder end(EndEvent end) {
      this.end = end;
      return this;
    }
  }
}
