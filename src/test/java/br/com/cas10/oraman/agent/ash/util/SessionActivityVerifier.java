package br.com.cas10.oraman.agent.ash.util;

import static br.com.cas10.oraman.agent.ash.util.Util.verifyMultiset;
import static org.junit.Assert.assertEquals;

import br.com.cas10.oraman.agent.ash.SessionActivity;

public class SessionActivityVerifier {

  private final int totalActivity;

  public SessionActivityVerifier(int totalActivity) {
    this.totalActivity = totalActivity;
  }

  public Verification verify(SessionActivity sqlActivity) {
    return new Verification(sqlActivity);
  }

  public class Verification {

    private final SessionActivity sessionActivity;

    private Verification(SessionActivity sessionActivity) {
      this.sessionActivity = sessionActivity;
    }

    public Verification session(String[] session) {
      assertEquals(session[0], sessionActivity.sessionId);
      assertEquals(session[1], sessionActivity.serialNumber);
      return this;
    }

    public Verification activity(int activity) {
      assertEquals(activity, sessionActivity.activity);
      assertEquals((100d * activity) / totalActivity, sessionActivity.percentageTotalActivity, 0);
      return this;
    }

    public Verification events(Object... events) {
      verifyMultiset(events, sessionActivity.activityByEvent);
      return this;
    }

    public Verification waitClasses(Object... waitClasses) {
      verifyMultiset(waitClasses, sessionActivity.activityByWaitClass);
      return this;
    }
  }
}
