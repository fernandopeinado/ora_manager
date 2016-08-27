package br.com.cas10.oraman.agent.ash;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Multisets.unmodifiableMultiset;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import br.com.cas10.oraman.oracle.data.ActiveSession;

public class SessionActivity {

  public final String sessionId;
  public final String serialNumber;
  public final String username;
  public final String program;
  public final int activity;
  public final double percentageTotalActivity;
  public final Multiset<String> activityByEvent;
  public final Multiset<String> activityByWaitClass;

  private SessionActivity(String sessionId, String serialNumber, String username, String program,
      Multiset<String> activityByEvent, Multiset<String> activityByWaitClass, int totalActivity) {
    this.sessionId = sessionId;
    this.serialNumber = serialNumber;
    this.username = username;
    this.program = program;
    this.activity = activityByWaitClass.size();
    this.percentageTotalActivity = (activity * 100d) / totalActivity;
    this.activityByEvent = unmodifiableMultiset(activityByEvent);
    this.activityByWaitClass = unmodifiableMultiset(activityByWaitClass);
  }

  public static class Builder {

    private final String sessionId;
    private final String serialNumber;
    private final String username;
    private final String program;
    private final Multiset<String> activityByEvent = HashMultiset.create();
    private final Multiset<String> activityByWaitClass = HashMultiset.create();

    public Builder(String sessionId, String serialNumber, String username, String program) {
      this.sessionId = checkNotNull(sessionId);
      this.serialNumber = checkNotNull(serialNumber);
      this.username = username;
      this.program = program;
    }

    public int getActivity() {
      return activityByWaitClass.size();
    }

    public void add(ActiveSession activeSession) {
      activityByEvent.add(activeSession.event);
      activityByWaitClass.add(activeSession.waitClass);
    }

    public SessionActivity build(int totalActivity) {
      return new SessionActivity(sessionId, serialNumber, username, program, activityByEvent,
          activityByWaitClass, totalActivity);
    }
  }
}
