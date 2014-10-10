package br.com.cas10.oraman.agent.ash;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class WaitEventActivity {

  public final String event;
  public final String waitClass;
  public final int activity;

  WaitEventActivity(String event, String waitClass, int activity) {
    checkArgument(activity >= 0);
    this.event = checkNotNull(event);
    this.waitClass = checkNotNull(waitClass);
    this.activity = activity;
  }
}
