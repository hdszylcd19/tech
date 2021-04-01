> API版本：28





## ContextImpl

### startActivity(Intent)

```java
	@Override
    public void startActivity(Intent intent) {
        warnIfCallingFromSystemProcess();
        startActivity(intent, null);
    }
```

### startActivity(Intent,Bundle)

```java
	@Override
    public void startActivity(Intent intent, Bundle options) {
        warnIfCallingFromSystemProcess();

        // Calling start activity from outside an activity without FLAG_ACTIVITY_NEW_TASK is
        // generally not allowed, except if the caller specifies the task id the activity should
        // be launched in. A bug was existed between N and O-MR1 which allowed this to work. We
        // maintain this for backwards compatibility.
        final int targetSdkVersion = getApplicationInfo().targetSdkVersion;

        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) == 0
                && (targetSdkVersion < Build.VERSION_CODES.N
                        || targetSdkVersion >= Build.VERSION_CODES.P)
                && (options == null
                        || ActivityOptions.fromBundle(options).getLaunchTaskId() == -1)) {
            throw new AndroidRuntimeException(
                    "Calling startActivity() from outside of an Activity "
                            + " context requires the FLAG_ACTIVITY_NEW_TASK flag."
                            + " Is this really what you want?");
        }
        mMainThread.getInstrumentation().execStartActivity(
                getOuterContext(), mMainThread.getApplicationThread(), null,
                (Activity) null, intent, -1, options);
    }
```

## Activity

### startActivity(Intent)

```java
	@Override
    public void startActivity(Intent intent) {
        this.startActivity(intent, null);
    }
```

### startActivity(Intent,Bundle)

```java
	@Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        if (options != null) {
            startActivityForResult(intent, -1, options);
        } else {
            // Note we want to go through this call for compatibility with
            // applications that may have overridden the method.
            startActivityForResult(intent, -1);
        }
    }
```

### startActivityForResult(Intent,int)

```java
public void startActivityForResult(@RequiresPermission Intent intent, int requestCode) {
	startActivityForResult(intent, requestCode, null);
}
```

### startActivityForResult(Intent,int,Bundle)

```java
public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
                                   @Nullable Bundle options) {
    if (mParent == null) {
        options = transferSpringboardActivityOptions(options);
        Instrumentation.ActivityResult ar =
            mInstrumentation.execStartActivity(
            this, mMainThread.getApplicationThread(), mToken, this,
            intent, requestCode, options);
        if (ar != null) {
            mMainThread.sendActivityResult(
                mToken, mEmbeddedID, requestCode, ar.getResultCode(),
                ar.getResultData());
        }
        if (requestCode >= 0) {
            // If this start is requesting a result, we can avoid making
            // the activity visible until the result is received.  Setting
            // this code during onCreate(Bundle savedInstanceState) or onResume() will keep the
            // activity hidden during this time, to avoid flickering.
            // This can only be done when a result is requested because
            // that guarantees we will get information back when the
            // activity is finished, no matter what happens to it.
            mStartedActivity = true;
        }

        cancelInputsAndStartExitTransition(options);
        // TODO Consider clearing/flushing other event sources and events for child windows.
    } else {
        if (options != null) {
            mParent.startActivityFromChild(this, intent, requestCode, options);
        } else {
            // Note we want to go through this method for compatibility with
            // existing applications that may have overridden it.
            mParent.startActivityFromChild(this, intent, requestCode);
        }
    }
}
```

## Instrumentation

### execStartActivity(Context,IBinder,IBinder,Activity,Intent,int,Bundle)

7个参数。

```java
	public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        Uri referrer = target != null ? target.onProvideReferrer() : null;
        if (referrer != null) {
            intent.putExtra(Intent.EXTRA_REFERRER, referrer);
        }
        if (mActivityMonitors != null) {
            synchronized (mSync) {
                final int N = mActivityMonitors.size();
                for (int i=0; i<N; i++) {
                    final ActivityMonitor am = mActivityMonitors.get(i);
                    ActivityResult result = null;
                    if (am.ignoreMatchingSpecificIntents()) {
                        result = am.onStartActivity(intent);
                    }
                    if (result != null) {
                        am.mHits++;
                        return result;
                    } else if (am.match(who, null, intent)) {
                        am.mHits++;
                        if (am.isBlocking()) {
                            return requestCode >= 0 ? am.getResult() : null;
                        }
                        break;
                    }
                }
            }
        }
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            int result = ActivityManager.getService()
                .startActivity(whoThread, who.getBasePackageName(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, target != null ? target.mEmbeddedID : null,
                        requestCode, 0, null, options);
            checkStartActivityResult(result, intent);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
        return null;
    }
```

## ActivityManagerService

### startActivity(IApplicationThread,String,Intent,String,IBinder,String,int,int, ProfilerInfo,Bundle)

10个参数。

```java
	@Override
    public final int startActivity(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo,resultWho, requestCode, startFlags, profilerInfo, bOptions,UserHandle.getCallingUserId());
    }
```

### startActivityAsUser(IApplicationThread,String,Intent,String,IBinder,String,int,int,ProfilerInfo,Bundle,int)

11个参数。

```java
	@Override
    public final int startActivityAsUser(IApplicationThread caller, String callingPackage,Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo,resultWho, requestCode, startFlags, profilerInfo, bOptions, userId,true /*validateIncomingUser*/);
    }
```

### startActivityAsUser(IApplicationThread,String,Intent,String,IBinder,String,int,int,ProfilerInfo,Bundle,int,boolean)
12个参数。

```java
public final int startActivityAsUser(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId,
            boolean validateIncomingUser) {
        enforceNotIsolatedCaller("startActivity");

        userId = mActivityStartController.checkTargetUser(userId, validateIncomingUser,
                Binder.getCallingPid(), Binder.getCallingUid(), "startActivityAsUser");

        // TODO: Switch to user app stacks here.
        return mActivityStartController.obtainStarter(intent, "startActivityAsUser")
                .setCaller(caller)
                .setCallingPackage(callingPackage)
                .setResolvedType(resolvedType)
                .setResultTo(resultTo)
                .setResultWho(resultWho)
                .setRequestCode(requestCode)
                .setStartFlags(startFlags)
                .setProfilerInfo(profilerInfo)
                .setActivityOptions(bOptions)
                .setMayWait(userId)
                .execute();

    }
```

## ActivityStarter

### execute()

```java
    int execute() {
        try {
            // TODO(b/64750076): Look into passing request directly to these methods to allow
            // for transactional diffs and preprocessing.
            if (mRequest.mayWait) {
                return startActivityMayWait(mRequest.caller, mRequest.callingUid,
                        mRequest.callingPackage, mRequest.intent, mRequest.resolvedType,
                        mRequest.voiceSession, mRequest.voiceInteractor, mRequest.resultTo,
                        mRequest.resultWho, mRequest.requestCode, mRequest.startFlags,
                        mRequest.profilerInfo, mRequest.waitResult, mRequest.globalConfig,
                        mRequest.activityOptions, mRequest.ignoreTargetSecurity, mRequest.userId,
                        mRequest.inTask, mRequest.reason,
                        mRequest.allowPendingRemoteAnimationRegistryLookup);
            } else {
                return startActivity(mRequest.caller, mRequest.intent, mRequest.ephemeralIntent,
                        mRequest.resolvedType, mRequest.activityInfo, mRequest.resolveInfo,
                        mRequest.voiceSession, mRequest.voiceInteractor, mRequest.resultTo,
                        mRequest.resultWho, mRequest.requestCode, mRequest.callingPid,
                        mRequest.callingUid, mRequest.callingPackage, mRequest.realCallingPid,
                        mRequest.realCallingUid, mRequest.startFlags, mRequest.activityOptions,
                        mRequest.ignoreTargetSecurity, mRequest.componentSpecified,
                        mRequest.outActivity, mRequest.inTask, mRequest.reason,
                        mRequest.allowPendingRemoteAnimationRegistryLookup);
            }
        } finally {
            onExecutionComplete();
        }
    }
```

### startActivityMayWait(IApplicationThread,int,String,Intent,String,IVoiceInteractionSession,IVoiceInteractor,IBinder,String,int,int,ProfilerInfo,WaitResult,Configuration,SafeActivityOptions,boolean,int,TaskRecord,String,boolean)

20个参数。

```java
    private int startActivityMayWait(IApplicationThread caller, int callingUid,
            String callingPackage, Intent intent, String resolvedType,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            IBinder resultTo, String resultWho, int requestCode, int startFlags,
            ProfilerInfo profilerInfo, WaitResult outResult,
            Configuration globalConfig, SafeActivityOptions options, boolean ignoreTargetSecurity,
            int userId, TaskRecord inTask, String reason,
            boolean allowPendingRemoteAnimationRegistryLookup) {
        // Refuse possible leaked file descriptors
        if (intent != null && intent.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        mSupervisor.getActivityMetricsLogger().notifyActivityLaunching();
        boolean componentSpecified = intent.getComponent() != null;

        final int realCallingPid = Binder.getCallingPid();
        final int realCallingUid = Binder.getCallingUid();

        int callingPid;
        if (callingUid >= 0) {
            callingPid = -1;
        } else if (caller == null) {
            callingPid = realCallingPid;
            callingUid = realCallingUid;
        } else {
            callingPid = callingUid = -1;
        }

        // Save a copy in case ephemeral needs it
        final Intent ephemeralIntent = new Intent(intent);
        // Don't modify the client's object!
        intent = new Intent(intent);
        if (componentSpecified
                && !(Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() == null)
                && !Intent.ACTION_INSTALL_INSTANT_APP_PACKAGE.equals(intent.getAction())
                && !Intent.ACTION_RESOLVE_INSTANT_APP_PACKAGE.equals(intent.getAction())
                && mService.getPackageManagerInternalLocked()
                        .isInstantAppInstallerComponent(intent.getComponent())) {
            // intercept intents targeted directly to the ephemeral installer the
            // ephemeral installer should never be started with a raw Intent; instead
            // adjust the intent so it looks like a "normal" instant app launch
            intent.setComponent(null /*component*/);
            componentSpecified = false;
        }

        ResolveInfo rInfo = mSupervisor.resolveIntent(intent, resolvedType, userId,
                0 /* matchFlags */,
                        computeResolveFilterUid(
                                callingUid, realCallingUid, mRequest.filterCallingUid));
        if (rInfo == null) {
            UserInfo userInfo = mSupervisor.getUserInfo(userId);
            if (userInfo != null && userInfo.isManagedProfile()) {
                // Special case for managed profiles, if attempting to launch non-cryto aware
                // app in a locked managed profile from an unlocked parent allow it to resolve
                // as user will be sent via confirm credentials to unlock the profile.
                UserManager userManager = UserManager.get(mService.mContext);
                boolean profileLockedAndParentUnlockingOrUnlocked = false;
                long token = Binder.clearCallingIdentity();
                try {
                    UserInfo parent = userManager.getProfileParent(userId);
                    profileLockedAndParentUnlockingOrUnlocked = (parent != null)
                            && userManager.isUserUnlockingOrUnlocked(parent.id)
                            && !userManager.isUserUnlockingOrUnlocked(userId);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
                if (profileLockedAndParentUnlockingOrUnlocked) {
                    rInfo = mSupervisor.resolveIntent(intent, resolvedType, userId,
                            PackageManager.MATCH_DIRECT_BOOT_AWARE
                                    | PackageManager.MATCH_DIRECT_BOOT_UNAWARE,
                            computeResolveFilterUid(
                                    callingUid, realCallingUid, mRequest.filterCallingUid));
                }
            }
        }
        // Collect information about the target of the Intent.
        ActivityInfo aInfo = mSupervisor.resolveActivity(intent, rInfo, startFlags, profilerInfo);

        synchronized (mService) {
            final ActivityStack stack = mSupervisor.mFocusedStack;
            stack.mConfigWillChange = globalConfig != null
                    && mService.getGlobalConfiguration().diff(globalConfig) != 0;
            if (DEBUG_CONFIGURATION) Slog.v(TAG_CONFIGURATION,
                    "Starting activity when config will change = " + stack.mConfigWillChange);

            final long origId = Binder.clearCallingIdentity();

            if (aInfo != null &&
                    (aInfo.applicationInfo.privateFlags
                            & ApplicationInfo.PRIVATE_FLAG_CANT_SAVE_STATE) != 0 &&
                    mService.mHasHeavyWeightFeature) {
                // This may be a heavy-weight process!  Check to see if we already
                // have another, different heavy-weight process running.
                if (aInfo.processName.equals(aInfo.applicationInfo.packageName)) {
                    final ProcessRecord heavy = mService.mHeavyWeightProcess;
                    if (heavy != null && (heavy.info.uid != aInfo.applicationInfo.uid
                            || !heavy.processName.equals(aInfo.processName))) {
                        int appCallingUid = callingUid;
                        if (caller != null) {
                            ProcessRecord callerApp = mService.getRecordForAppLocked(caller);
                            if (callerApp != null) {
                                appCallingUid = callerApp.info.uid;
                            } else {
                                Slog.w(TAG, "Unable to find app for caller " + caller
                                        + " (pid=" + callingPid + ") when starting: "
                                        + intent.toString());
                                SafeActivityOptions.abort(options);
                                return ActivityManager.START_PERMISSION_DENIED;
                            }
                        }

                        IIntentSender target = mService.getIntentSenderLocked(
                                ActivityManager.INTENT_SENDER_ACTIVITY, "android",
                                appCallingUid, userId, null, null, 0, new Intent[] { intent },
                                new String[] { resolvedType }, PendingIntent.FLAG_CANCEL_CURRENT
                                        | PendingIntent.FLAG_ONE_SHOT, null);

                        Intent newIntent = new Intent();
                        if (requestCode >= 0) {
                            // Caller is requesting a result.
                            newIntent.putExtra(HeavyWeightSwitcherActivity.KEY_HAS_RESULT, true);
                        }
                        newIntent.putExtra(HeavyWeightSwitcherActivity.KEY_INTENT,
                                new IntentSender(target));
                        if (heavy.activities.size() > 0) {
                            ActivityRecord hist = heavy.activities.get(0);
                            newIntent.putExtra(HeavyWeightSwitcherActivity.KEY_CUR_APP,
                                    hist.packageName);
                            newIntent.putExtra(HeavyWeightSwitcherActivity.KEY_CUR_TASK,
                                    hist.getTask().taskId);
                        }
                        newIntent.putExtra(HeavyWeightSwitcherActivity.KEY_NEW_APP,
                                aInfo.packageName);
                        newIntent.setFlags(intent.getFlags());
                        newIntent.setClassName("android",
                                HeavyWeightSwitcherActivity.class.getName());
                        intent = newIntent;
                        resolvedType = null;
                        caller = null;
                        callingUid = Binder.getCallingUid();
                        callingPid = Binder.getCallingPid();
                        componentSpecified = true;
                        rInfo = mSupervisor.resolveIntent(intent, null /*resolvedType*/, userId,
                                0 /* matchFlags */, computeResolveFilterUid(
                                        callingUid, realCallingUid, mRequest.filterCallingUid));
                        aInfo = rInfo != null ? rInfo.activityInfo : null;
                        if (aInfo != null) {
                            aInfo = mService.getActivityInfoForUser(aInfo, userId);
                        }
                    }
                }
            }

            final ActivityRecord[] outRecord = new ActivityRecord[1];
            int res = startActivity(caller, intent, ephemeralIntent, resolvedType, aInfo, rInfo,
                    voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid,
                    callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options,
                    ignoreTargetSecurity, componentSpecified, outRecord, inTask, reason,
                    allowPendingRemoteAnimationRegistryLookup);

            Binder.restoreCallingIdentity(origId);

            if (stack.mConfigWillChange) {
                // If the caller also wants to switch to a new configuration,
                // do so now.  This allows a clean switch, as we are waiting
                // for the current activity to pause (so we will not destroy
                // it), and have not yet started the next activity.
                mService.enforceCallingPermission(android.Manifest.permission.CHANGE_CONFIGURATION,
                        "updateConfiguration()");
                stack.mConfigWillChange = false;
                if (DEBUG_CONFIGURATION) Slog.v(TAG_CONFIGURATION,
                        "Updating to new configuration after starting activity.");
                mService.updateConfigurationLocked(globalConfig, null, false);
            }

            if (outResult != null) {
                outResult.result = res;

                final ActivityRecord r = outRecord[0];

                switch(res) {
                    case START_SUCCESS: {
                        mSupervisor.mWaitingActivityLaunched.add(outResult);
                        do {
                            try {
                                mService.wait();
                            } catch (InterruptedException e) {
                            }
                        } while (outResult.result != START_TASK_TO_FRONT
                                && !outResult.timeout && outResult.who == null);
                        if (outResult.result == START_TASK_TO_FRONT) {
                            res = START_TASK_TO_FRONT;
                        }
                        break;
                    }
                    case START_DELIVERED_TO_TOP: {
                        outResult.timeout = false;
                        outResult.who = r.realActivity;
                        outResult.totalTime = 0;
                        outResult.thisTime = 0;
                        break;
                    }
                    case START_TASK_TO_FRONT: {
                        // ActivityRecord may represent a different activity, but it should not be
                        // in the resumed state.
                        if (r.nowVisible && r.isState(RESUMED)) {
                            outResult.timeout = false;
                            outResult.who = r.realActivity;
                            outResult.totalTime = 0;
                            outResult.thisTime = 0;
                        } else {
                            outResult.thisTime = SystemClock.uptimeMillis();
                            mSupervisor.waitActivityVisible(r.realActivity, outResult);
                            // Note: the timeout variable is not currently not ever set.
                            do {
                                try {
                                    mService.wait();
                                } catch (InterruptedException e) {
                                }
                            } while (!outResult.timeout && outResult.who == null);
                        }
                        break;
                    }
                }
            }

            mSupervisor.getActivityMetricsLogger().notifyActivityLaunched(res, outRecord[0]);
            return res;
        }
    }
```

### startActivity(IApplicationThread,Intent,Intent,String,ActivityInfo,ResolveInfo,IVoiceInteractionSession,IVoiceInteractor,IBinder,String,int,int,int,String,int,int,int,SafeActivityOptions,boolean,boolean,ActivityRecord[],TaskRecord,String,boolean)

24个参数。

```java
	private int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent,
            String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid,
            String callingPackage, int realCallingPid, int realCallingUid, int startFlags,
            SafeActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified,
            ActivityRecord[] outActivity, TaskRecord inTask, String reason,
            boolean allowPendingRemoteAnimationRegistryLookup) {

        if (TextUtils.isEmpty(reason)) {
            throw new IllegalArgumentException("Need to specify a reason.");
        }
        mLastStartReason = reason;
        mLastStartActivityTimeMs = System.currentTimeMillis();
        mLastStartActivityRecord[0] = null;

        mLastStartActivityResult = startActivity(caller, intent, ephemeralIntent, resolvedType,
                aInfo, rInfo, voiceSession, voiceInteractor, resultTo, resultWho, requestCode,
                callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags,
                options, ignoreTargetSecurity, componentSpecified, mLastStartActivityRecord,
                inTask, allowPendingRemoteAnimationRegistryLookup);

        if (outActivity != null) {
            // mLastStartActivityRecord[0] is set in the call to startActivity above.
            outActivity[0] = mLastStartActivityRecord[0];
        }

        return getExternalResult(mLastStartActivityResult);
    }
```

### startActivity(IApplicationThread,Intent,Intent,String,ActivityInfo,ResolveInfo,IVoiceInteractionSession,IVoiceInteractor,IBinder,String,int,int,int,String,int,int,int,SafeActivityOptions,boolean,boolean,ActivityRecord[],TaskRecord,boolean)

23个参数。

```java
private int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent,
            String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid,
            String callingPackage, int realCallingPid, int realCallingUid, int startFlags,
            SafeActivityOptions options,
            boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity,
            TaskRecord inTask, boolean allowPendingRemoteAnimationRegistryLookup) {
        int err = ActivityManager.START_SUCCESS;
        // Pull the optional Ephemeral Installer-only bundle out of the options early.
        final Bundle verificationBundle
                = options != null ? options.popAppVerificationBundle() : null;

        ProcessRecord callerApp = null;
        if (caller != null) {
            callerApp = mService.getRecordForAppLocked(caller);
            if (callerApp != null) {
                callingPid = callerApp.pid;
                callingUid = callerApp.info.uid;
            } else {
                Slog.w(TAG, "Unable to find app for caller " + caller
                        + " (pid=" + callingPid + ") when starting: "
                        + intent.toString());
                err = ActivityManager.START_PERMISSION_DENIED;
            }
        }

        final int userId = aInfo != null && aInfo.applicationInfo != null
                ? UserHandle.getUserId(aInfo.applicationInfo.uid) : 0;

        if (err == ActivityManager.START_SUCCESS) {
            Slog.i(TAG, "START u" + userId + " {" + intent.toShortString(true, true, true, false)
                    + "} from uid " + callingUid);
        }

        ActivityRecord sourceRecord = null;
        ActivityRecord resultRecord = null;
        if (resultTo != null) {
            sourceRecord = mSupervisor.isInAnyStackLocked(resultTo);
            if (DEBUG_RESULTS) Slog.v(TAG_RESULTS,
                    "Will send result to " + resultTo + " " + sourceRecord);
            if (sourceRecord != null) {
                if (requestCode >= 0 && !sourceRecord.finishing) {
                    resultRecord = sourceRecord;
                }
            }
        }

        final int launchFlags = intent.getFlags();

        if ((launchFlags & Intent.FLAG_ACTIVITY_FORWARD_RESULT) != 0 && sourceRecord != null) {
            // Transfer the result target from the source activity to the new
            // one being started, including any failures.
            if (requestCode >= 0) {
                SafeActivityOptions.abort(options);
                return ActivityManager.START_FORWARD_AND_REQUEST_CONFLICT;
            }
            resultRecord = sourceRecord.resultTo;
            if (resultRecord != null && !resultRecord.isInStackLocked()) {
                resultRecord = null;
            }
            resultWho = sourceRecord.resultWho;
            requestCode = sourceRecord.requestCode;
            sourceRecord.resultTo = null;
            if (resultRecord != null) {
                resultRecord.removeResultsLocked(sourceRecord, resultWho, requestCode);
            }
            if (sourceRecord.launchedFromUid == callingUid) {
                // The new activity is being launched from the same uid as the previous
                // activity in the flow, and asking to forward its result back to the
                // previous.  In this case the activity is serving as a trampoline between
                // the two, so we also want to update its launchedFromPackage to be the
                // same as the previous activity.  Note that this is safe, since we know
                // these two packages come from the same uid; the caller could just as
                // well have supplied that same package name itself.  This specifially
                // deals with the case of an intent picker/chooser being launched in the app
                // flow to redirect to an activity picked by the user, where we want the final
                // activity to consider it to have been launched by the previous app activity.
                callingPackage = sourceRecord.launchedFromPackage;
            }
        }

        if (err == ActivityManager.START_SUCCESS && intent.getComponent() == null) {
            // We couldn't find a class that can handle the given Intent.
            // That's the end of that!
            err = ActivityManager.START_INTENT_NOT_RESOLVED;
        }

        if (err == ActivityManager.START_SUCCESS && aInfo == null) {
            // We couldn't find the specific class specified in the Intent.
            // Also the end of the line.
            err = ActivityManager.START_CLASS_NOT_FOUND;
        }

        if (err == ActivityManager.START_SUCCESS && sourceRecord != null
                && sourceRecord.getTask().voiceSession != null) {
            // If this activity is being launched as part of a voice session, we need
            // to ensure that it is safe to do so.  If the upcoming activity will also
            // be part of the voice session, we can only launch it if it has explicitly
            // said it supports the VOICE category, or it is a part of the calling app.
            if ((launchFlags & FLAG_ACTIVITY_NEW_TASK) == 0
                    && sourceRecord.info.applicationInfo.uid != aInfo.applicationInfo.uid) {
                try {
                    intent.addCategory(Intent.CATEGORY_VOICE);
                    if (!mService.getPackageManager().activitySupportsIntent(
                            intent.getComponent(), intent, resolvedType)) {
                        Slog.w(TAG,
                                "Activity being started in current voice task does not support voice: "
                                        + intent);
                        err = ActivityManager.START_NOT_VOICE_COMPATIBLE;
                    }
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failure checking voice capabilities", e);
                    err = ActivityManager.START_NOT_VOICE_COMPATIBLE;
                }
            }
        }

        if (err == ActivityManager.START_SUCCESS && voiceSession != null) {
            // If the caller is starting a new voice session, just make sure the target
            // is actually allowing it to run this way.
            try {
                if (!mService.getPackageManager().activitySupportsIntent(intent.getComponent(),
                        intent, resolvedType)) {
                    Slog.w(TAG,
                            "Activity being started in new voice task does not support: "
                                    + intent);
                    err = ActivityManager.START_NOT_VOICE_COMPATIBLE;
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Failure checking voice capabilities", e);
                err = ActivityManager.START_NOT_VOICE_COMPATIBLE;
            }
        }

        final ActivityStack resultStack = resultRecord == null ? null : resultRecord.getStack();

        if (err != START_SUCCESS) {
            if (resultRecord != null) {
                resultStack.sendActivityResultLocked(
                        -1, resultRecord, resultWho, requestCode, RESULT_CANCELED, null);
            }
            SafeActivityOptions.abort(options);
            return err;
        }

        boolean abort = !mSupervisor.checkStartAnyActivityPermission(intent, aInfo, resultWho,
                requestCode, callingPid, callingUid, callingPackage, ignoreTargetSecurity,
                inTask != null, callerApp, resultRecord, resultStack);
        abort |= !mService.mIntentFirewall.checkStartActivity(intent, callingUid,
                callingPid, resolvedType, aInfo.applicationInfo);

        // Merge the two options bundles, while realCallerOptions takes precedence.
        ActivityOptions checkedOptions = options != null
                ? options.getOptions(intent, aInfo, callerApp, mSupervisor)
                : null;
        if (allowPendingRemoteAnimationRegistryLookup) {
            checkedOptions = mService.getActivityStartController()
                    .getPendingRemoteAnimationRegistry()
                    .overrideOptionsIfNeeded(callingPackage, checkedOptions);
        }
        if (mService.mController != null) {
            try {
                // The Intent we give to the watcher has the extra data
                // stripped off, since it can contain private information.
                Intent watchIntent = intent.cloneFilter();
                abort |= !mService.mController.activityStarting(watchIntent,
                        aInfo.applicationInfo.packageName);
            } catch (RemoteException e) {
                mService.mController = null;
            }
        }

        mInterceptor.setStates(userId, realCallingPid, realCallingUid, startFlags, callingPackage);
        if (mInterceptor.intercept(intent, rInfo, aInfo, resolvedType, inTask, callingPid,
                callingUid, checkedOptions)) {
            // activity start was intercepted, e.g. because the target user is currently in quiet
            // mode (turn off work) or the target application is suspended
            intent = mInterceptor.mIntent;
            rInfo = mInterceptor.mRInfo;
            aInfo = mInterceptor.mAInfo;
            resolvedType = mInterceptor.mResolvedType;
            inTask = mInterceptor.mInTask;
            callingPid = mInterceptor.mCallingPid;
            callingUid = mInterceptor.mCallingUid;
            checkedOptions = mInterceptor.mActivityOptions;
        }

        if (abort) {
            if (resultRecord != null) {
                resultStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode,
                        RESULT_CANCELED, null);
            }
            // We pretend to the caller that it was really started, but
            // they will just get a cancel result.
            ActivityOptions.abort(checkedOptions);
            return START_ABORTED;
        }

        // If permissions need a review before any of the app components can run, we
        // launch the review activity and pass a pending intent to start the activity
        // we are to launching now after the review is completed.
        if (mService.mPermissionReviewRequired && aInfo != null) {
            if (mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(
                    aInfo.packageName, userId)) {
                IIntentSender target = mService.getIntentSenderLocked(
                        ActivityManager.INTENT_SENDER_ACTIVITY, callingPackage,
                        callingUid, userId, null, null, 0, new Intent[]{intent},
                        new String[]{resolvedType}, PendingIntent.FLAG_CANCEL_CURRENT
                                | PendingIntent.FLAG_ONE_SHOT, null);

                final int flags = intent.getFlags();
                Intent newIntent = new Intent(Intent.ACTION_REVIEW_PERMISSIONS);
                newIntent.setFlags(flags
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                newIntent.putExtra(Intent.EXTRA_PACKAGE_NAME, aInfo.packageName);
                newIntent.putExtra(Intent.EXTRA_INTENT, new IntentSender(target));
                if (resultRecord != null) {
                    newIntent.putExtra(Intent.EXTRA_RESULT_NEEDED, true);
                }
                intent = newIntent;

                resolvedType = null;
                callingUid = realCallingUid;
                callingPid = realCallingPid;

                rInfo = mSupervisor.resolveIntent(intent, resolvedType, userId, 0,
                        computeResolveFilterUid(
                                callingUid, realCallingUid, mRequest.filterCallingUid));
                aInfo = mSupervisor.resolveActivity(intent, rInfo, startFlags,
                        null /*profilerInfo*/);

                if (DEBUG_PERMISSIONS_REVIEW) {
                    Slog.i(TAG, "START u" + userId + " {" + intent.toShortString(true, true,
                            true, false) + "} from uid " + callingUid + " on display "
                            + (mSupervisor.mFocusedStack == null
                            ? DEFAULT_DISPLAY : mSupervisor.mFocusedStack.mDisplayId));
                }
            }
        }

        // If we have an ephemeral app, abort the process of launching the resolved intent.
        // Instead, launch the ephemeral installer. Once the installer is finished, it
        // starts either the intent we resolved here [on install error] or the ephemeral
        // app [on install success].
        if (rInfo != null && rInfo.auxiliaryInfo != null) {
            intent = createLaunchIntent(rInfo.auxiliaryInfo, ephemeralIntent,
                    callingPackage, verificationBundle, resolvedType, userId);
            resolvedType = null;
            callingUid = realCallingUid;
            callingPid = realCallingPid;

            aInfo = mSupervisor.resolveActivity(intent, rInfo, startFlags, null /*profilerInfo*/);
        }

        ActivityRecord r = new ActivityRecord(mService, callerApp, callingPid, callingUid,
                callingPackage, intent, resolvedType, aInfo, mService.getGlobalConfiguration(),
                resultRecord, resultWho, requestCode, componentSpecified, voiceSession != null,
                mSupervisor, checkedOptions, sourceRecord);
        if (outActivity != null) {
            outActivity[0] = r;
        }

        if (r.appTimeTracker == null && sourceRecord != null) {
            // If the caller didn't specify an explicit time tracker, we want to continue
            // tracking under any it has.
            r.appTimeTracker = sourceRecord.appTimeTracker;
        }

        final ActivityStack stack = mSupervisor.mFocusedStack;

        // If we are starting an activity that is not from the same uid as the currently resumed
        // one, check whether app switches are allowed.
        if (voiceSession == null && (stack.getResumedActivity() == null
                || stack.getResumedActivity().info.applicationInfo.uid != realCallingUid)) {
            if (!mService.checkAppSwitchAllowedLocked(callingPid, callingUid,
                    realCallingPid, realCallingUid, "Activity start")) {
                mController.addPendingActivityLaunch(new PendingActivityLaunch(r,
                        sourceRecord, startFlags, stack, callerApp));
                ActivityOptions.abort(checkedOptions);
                return ActivityManager.START_SWITCHES_CANCELED;
            }
        }

        if (mService.mDidAppSwitch) {
            // This is the second allowed switch since we stopped switches,
            // so now just generally allow switches.  Use case: user presses
            // home (switches disabled, switch to home, mDidAppSwitch now true);
            // user taps a home icon (coming from home so allowed, we hit here
            // and now allow anyone to switch again).
            mService.mAppSwitchesAllowedTime = 0;
        } else {
            mService.mDidAppSwitch = true;
        }

        mController.doPendingActivityLaunches(false);

        return startActivity(r, sourceRecord, voiceSession, voiceInteractor, startFlags,
                true /* doResume */, checkedOptions, inTask, outActivity);
    }
```

### startActivity(ActivityRecord,ActivityRecord,IVoiceInteractionSession,IVoiceInteractor,int,boolean,ActivityOptions,TaskRecord,ActivityRecord[])

9个参数。

```java
	private int startActivity(final ActivityRecord r, ActivityRecord sourceRecord,
                IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
                int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask,
                ActivityRecord[] outActivity) {
        int result = START_CANCELED;
        try {
            mService.mWindowManager.deferSurfaceLayout();
            result = startActivityUnchecked(r, sourceRecord, voiceSession, voiceInteractor,
                    startFlags, doResume, options, inTask, outActivity);
        } finally {
            // If we are not able to proceed, disassociate the activity from the task. Leaving an
            // activity in an incomplete state can lead to issues, such as performing operations
            // without a window container.
            final ActivityStack stack = mStartActivity.getStack();
            if (!ActivityManager.isStartResultSuccessful(result) && stack != null) {
                stack.finishActivityLocked(mStartActivity, RESULT_CANCELED,
                        null /* intentResultData */, "startActivity", true /* oomAdj */);
            }
            mService.mWindowManager.continueSurfaceLayout();
        }

        postStartActivityProcessing(r, result, mTargetStack);

        return result;
    }
```

### startActivityUnchecked(ActivityRecord,ActivityRecord,IVoiceInteractionSession,IVoiceInteractor,int,boolean,ActivityOptions,TaskRecord,ActivityRecord[])

9个参数。

```java
    // Note: This method should only be called from {@link startActivity}.
    private int startActivityUnchecked(final ActivityRecord r, ActivityRecord sourceRecord,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask,
            ActivityRecord[] outActivity) {

        setInitialState(r, options, inTask, doResume, startFlags, sourceRecord, voiceSession,
                voiceInteractor);

        computeLaunchingTaskFlags();

        computeSourceStack();

        mIntent.setFlags(mLaunchFlags);

        ActivityRecord reusedActivity = getReusableIntentActivity();

        int preferredWindowingMode = WINDOWING_MODE_UNDEFINED;
        int preferredLaunchDisplayId = DEFAULT_DISPLAY;
        if (mOptions != null) {
            preferredWindowingMode = mOptions.getLaunchWindowingMode();
            preferredLaunchDisplayId = mOptions.getLaunchDisplayId();
        }

        // windowing mode and preferred launch display values from {@link LaunchParams} take
        // priority over those specified in {@link ActivityOptions}.
        if (!mLaunchParams.isEmpty()) {
            if (mLaunchParams.hasPreferredDisplay()) {
                preferredLaunchDisplayId = mLaunchParams.mPreferredDisplayId;
            }

            if (mLaunchParams.hasWindowingMode()) {
                preferredWindowingMode = mLaunchParams.mWindowingMode;
            }
        }

        if (reusedActivity != null) {
            // When the flags NEW_TASK and CLEAR_TASK are set, then the task gets reused but
            // still needs to be a lock task mode violation since the task gets cleared out and
            // the device would otherwise leave the locked task.
            if (mService.getLockTaskController().isLockTaskModeViolation(reusedActivity.getTask(),
                    (mLaunchFlags & (FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK))
                            == (FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK))) {
                Slog.e(TAG, "startActivityUnchecked: Attempt to violate Lock Task Mode");
                return START_RETURN_LOCK_TASK_MODE_VIOLATION;
            }

            // True if we are clearing top and resetting of a standard (default) launch mode
            // ({@code LAUNCH_MULTIPLE}) activity. The existing activity will be finished.
            final boolean clearTopAndResetStandardLaunchMode =
                    (mLaunchFlags & (FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED))
                            == (FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    && mLaunchMode == LAUNCH_MULTIPLE;

            // If mStartActivity does not have a task associated with it, associate it with the
            // reused activity's task. Do not do so if we're clearing top and resetting for a
            // standard launchMode activity.
            if (mStartActivity.getTask() == null && !clearTopAndResetStandardLaunchMode) {
                mStartActivity.setTask(reusedActivity.getTask());
            }

            if (reusedActivity.getTask().intent == null) {
                // This task was started because of movement of the activity based on affinity...
                // Now that we are actually launching it, we can assign the base intent.
                reusedActivity.getTask().setIntent(mStartActivity);
            }

            // This code path leads to delivering a new intent, we want to make sure we schedule it
            // as the first operation, in case the activity will be resumed as a result of later
            // operations.
            if ((mLaunchFlags & FLAG_ACTIVITY_CLEAR_TOP) != 0
                    || isDocumentLaunchesIntoExisting(mLaunchFlags)
                    || isLaunchModeOneOf(LAUNCH_SINGLE_INSTANCE, LAUNCH_SINGLE_TASK)) {
                final TaskRecord task = reusedActivity.getTask();

                // In this situation we want to remove all activities from the task up to the one
                // being started. In most cases this means we are resetting the task to its initial
                // state.
                final ActivityRecord top = task.performClearTaskForReuseLocked(mStartActivity,
                        mLaunchFlags);

                // The above code can remove {@code reusedActivity} from the task, leading to the
                // the {@code ActivityRecord} removing its reference to the {@code TaskRecord}. The
                // task reference is needed in the call below to
                // {@link setTargetStackAndMoveToFrontIfNeeded}.
                if (reusedActivity.getTask() == null) {
                    reusedActivity.setTask(task);
                }

                if (top != null) {
                    if (top.frontOfTask) {
                        // Activity aliases may mean we use different intents for the top activity,
                        // so make sure the task now has the identity of the new intent.
                        top.getTask().setIntent(mStartActivity);
                    }
                    deliverNewIntent(top);
                }
            }

            mSupervisor.sendPowerHintForLaunchStartIfNeeded(false /* forceSend */, reusedActivity);

            reusedActivity = setTargetStackAndMoveToFrontIfNeeded(reusedActivity);

            final ActivityRecord outResult =
                    outActivity != null && outActivity.length > 0 ? outActivity[0] : null;

            // When there is a reused activity and the current result is a trampoline activity,
            // set the reused activity as the result.
            if (outResult != null && (outResult.finishing || outResult.noDisplay)) {
                outActivity[0] = reusedActivity;
            }

            if ((mStartFlags & START_FLAG_ONLY_IF_NEEDED) != 0) {
                // We don't need to start a new activity, and the client said not to do anything
                // if that is the case, so this is it!  And for paranoia, make sure we have
                // correctly resumed the top activity.
                resumeTargetStackIfNeeded();
                return START_RETURN_INTENT_TO_CALLER;
            }

            if (reusedActivity != null) {
                setTaskFromIntentActivity(reusedActivity);

                if (!mAddingToTask && mReuseTask == null) {
                    // We didn't do anything...  but it was needed (a.k.a., client don't use that
                    // intent!)  And for paranoia, make sure we have correctly resumed the top activity.

                    resumeTargetStackIfNeeded();
                    if (outActivity != null && outActivity.length > 0) {
                        outActivity[0] = reusedActivity;
                    }

                    return mMovedToFront ? START_TASK_TO_FRONT : START_DELIVERED_TO_TOP;
                }
            }
        }

        if (mStartActivity.packageName == null) {
            final ActivityStack sourceStack = mStartActivity.resultTo != null
                    ? mStartActivity.resultTo.getStack() : null;
            if (sourceStack != null) {
                sourceStack.sendActivityResultLocked(-1 /* callingUid */, mStartActivity.resultTo,
                        mStartActivity.resultWho, mStartActivity.requestCode, RESULT_CANCELED,
                        null /* data */);
            }
            ActivityOptions.abort(mOptions);
            return START_CLASS_NOT_FOUND;
        }

        // If the activity being launched is the same as the one currently at the top, then
        // we need to check if it should only be launched once.
        final ActivityStack topStack = mSupervisor.mFocusedStack;
        final ActivityRecord topFocused = topStack.getTopActivity();
        final ActivityRecord top = topStack.topRunningNonDelayedActivityLocked(mNotTop);
        final boolean dontStart = top != null && mStartActivity.resultTo == null
                && top.realActivity.equals(mStartActivity.realActivity)
                && top.userId == mStartActivity.userId
                && top.app != null && top.app.thread != null
                && ((mLaunchFlags & FLAG_ACTIVITY_SINGLE_TOP) != 0
                || isLaunchModeOneOf(LAUNCH_SINGLE_TOP, LAUNCH_SINGLE_TASK));
        if (dontStart) {
            // For paranoia, make sure we have correctly resumed the top activity.
            topStack.mLastPausedActivity = null;
            if (mDoResume) {
                mSupervisor.resumeFocusedStackTopActivityLocked();
            }
            ActivityOptions.abort(mOptions);
            if ((mStartFlags & START_FLAG_ONLY_IF_NEEDED) != 0) {
                // We don't need to start a new activity, and the client said not to do
                // anything if that is the case, so this is it!
                return START_RETURN_INTENT_TO_CALLER;
            }

            deliverNewIntent(top);

            // Don't use mStartActivity.task to show the toast. We're not starting a new activity
            // but reusing 'top'. Fields in mStartActivity may not be fully initialized.
            mSupervisor.handleNonResizableTaskIfNeeded(top.getTask(), preferredWindowingMode,
                    preferredLaunchDisplayId, topStack);

            return START_DELIVERED_TO_TOP;
        }

        boolean newTask = false;
        final TaskRecord taskToAffiliate = (mLaunchTaskBehind && mSourceRecord != null)
                ? mSourceRecord.getTask() : null;

        // Should this be considered a new task?
        int result = START_SUCCESS;
        if (mStartActivity.resultTo == null && mInTask == null && !mAddingToTask
                && (mLaunchFlags & FLAG_ACTIVITY_NEW_TASK) != 0) {
            newTask = true;
            result = setTaskFromReuseOrCreateNewTask(taskToAffiliate, topStack);
        } else if (mSourceRecord != null) {
            result = setTaskFromSourceRecord();
        } else if (mInTask != null) {
            result = setTaskFromInTask();
        } else {
            // This not being started from an existing activity, and not part of a new task...
            // just put it in the top task, though these days this case should never happen.
            setTaskToCurrentTopOrCreateNewTask();
        }
        if (result != START_SUCCESS) {
            return result;
        }

        mService.grantUriPermissionFromIntentLocked(mCallingUid, mStartActivity.packageName,
                mIntent, mStartActivity.getUriPermissionsLocked(), mStartActivity.userId);
        mService.grantEphemeralAccessLocked(mStartActivity.userId, mIntent,
                mStartActivity.appInfo.uid, UserHandle.getAppId(mCallingUid));
        if (newTask) {
            EventLog.writeEvent(EventLogTags.AM_CREATE_TASK, mStartActivity.userId,
                    mStartActivity.getTask().taskId);
        }
        ActivityStack.logStartActivity(
                EventLogTags.AM_CREATE_ACTIVITY, mStartActivity, mStartActivity.getTask());
        mTargetStack.mLastPausedActivity = null;

        mSupervisor.sendPowerHintForLaunchStartIfNeeded(false /* forceSend */, mStartActivity);

        mTargetStack.startActivityLocked(mStartActivity, topFocused, newTask, mKeepCurTransition,
                mOptions);
        if (mDoResume) {
            final ActivityRecord topTaskActivity =
                    mStartActivity.getTask().topRunningActivityLocked();
            if (!mTargetStack.isFocusable()
                    || (topTaskActivity != null && topTaskActivity.mTaskOverlay
                    && mStartActivity != topTaskActivity)) {
                // If the activity is not focusable, we can't resume it, but still would like to
                // make sure it becomes visible as it starts (this will also trigger entry
                // animation). An example of this are PIP activities.
                // Also, we don't want to resume activities in a task that currently has an overlay
                // as the starting activity just needs to be in the visible paused state until the
                // over is removed.
                mTargetStack.ensureActivitiesVisibleLocked(null, 0, !PRESERVE_WINDOWS);
                // Go ahead and tell window manager to execute app transition for this activity
                // since the app transition will not be triggered through the resume channel.
                mService.mWindowManager.executeAppTransition();
            } else {
                // If the target stack was not previously focusable (previous top running activity
                // on that stack was not visible) then any prior calls to move the stack to the
                // will not update the focused stack.  If starting the new activity now allows the
                // task stack to be focusable, then ensure that we now update the focused stack
                // accordingly.
                if (mTargetStack.isFocusable() && !mSupervisor.isFocusedStack(mTargetStack)) {
                    mTargetStack.moveToFront("startActivityUnchecked");
                }
                mSupervisor.resumeFocusedStackTopActivityLocked(mTargetStack, mStartActivity,
                        mOptions);
            }
        } else if (mStartActivity != null) {
            mSupervisor.mRecentTasks.add(mStartActivity.getTask());
        }
        mSupervisor.updateUserStackLocked(mStartActivity.userId, mTargetStack);

        mSupervisor.handleNonResizableTaskIfNeeded(mStartActivity.getTask(), preferredWindowingMode,
                preferredLaunchDisplayId, mTargetStack);

        return START_SUCCESS;
    }
```

## ActivityStackSupervisor

### resumeFocusedStackTopActivityLocked(ActivityStack,ActivityRecord,ActivityOptions)

3个参数。

```java
	boolean resumeFocusedStackTopActivityLocked(
            ActivityStack targetStack, ActivityRecord target, ActivityOptions targetOptions) {

        if (!readyToResume()) {
            return false;
        }

        if (targetStack != null && isFocusedStack(targetStack)) {
            return targetStack.resumeTopActivityUncheckedLocked(target, targetOptions);
        }

        final ActivityRecord r = mFocusedStack.topRunningActivityLocked();
        if (r == null || !r.isState(RESUMED)) {
            mFocusedStack.resumeTopActivityUncheckedLocked(null, null);
        } else if (r.isState(RESUMED)) {
            // Kick off any lingering app transitions form the MoveTaskToFront operation.
            mFocusedStack.executeAppTransition(targetOptions);
        }

        return false;
    }
```

## ActivityStack

### resumeTopActivityUncheckedLocked(ActivityRecord,ActivityOptions)

2个参数。

```java
    @GuardedBy("mService")
    boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options) {
        if (mStackSupervisor.inResumeTopActivity) {
            // Don't even start recursing.
            return false;
        }

        boolean result = false;
        try {
            // Protect against recursion.
            mStackSupervisor.inResumeTopActivity = true;
            result = resumeTopActivityInnerLocked(prev, options);

            // When resuming the top activity, it may be necessary to pause the top activity (for
            // example, returning to the lock screen. We suppress the normal pause logic in
            // {@link #resumeTopActivityUncheckedLocked}, since the top activity is resumed at the
            // end. We call the {@link ActivityStackSupervisor#checkReadyForSleepLocked} again here
            // to ensure any necessary pause logic occurs. In the case where the Activity will be
            // shown regardless of the lock screen, the call to
            // {@link ActivityStackSupervisor#checkReadyForSleepLocked} is skipped.
            final ActivityRecord next = topRunningActivityLocked(true /* focusableOnly */);
            if (next == null || !next.canTurnScreenOn()) {
                checkReadyForSleep();
            }
        } finally {
            mStackSupervisor.inResumeTopActivity = false;
        }

        return result;
    }
```

### resumeTopActivityInnerLocked(ActivityRecord,ActivityOptions)

2个参数。

```java
    @GuardedBy("mService")
    private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options) {
        if (!mService.mBooting && !mService.mBooted) {
            // Not ready yet!
            return false;
        }

        // Find the next top-most activity to resume in this stack that is not finishing and is
        // focusable. If it is not focusable, we will fall into the case below to resume the
        // top activity in the next focusable task.
        final ActivityRecord next = topRunningActivityLocked(true /* focusableOnly */);

        final boolean hasRunningActivity = next != null;

        // TODO: Maybe this entire condition can get removed?
        if (hasRunningActivity && !isAttached()) {
            return false;
        }

        mStackSupervisor.cancelInitializingActivities();

        // Remember how we'll process this pause/resume situation, and ensure
        // that the state is reset however we wind up proceeding.
        boolean userLeaving = mStackSupervisor.mUserLeaving;
        mStackSupervisor.mUserLeaving = false;

        if (!hasRunningActivity) {
            // There are no activities left in the stack, let's look somewhere else.
            return resumeTopActivityInNextFocusableStack(prev, options, "noMoreActivities");
        }

        next.delayedResume = false;

        // If the top activity is the resumed one, nothing to do.
        if (mResumedActivity == next && next.isState(RESUMED)
                && mStackSupervisor.allResumedActivitiesComplete()) {
            // Make sure we have executed any pending transitions, since there
            // should be nothing left to do at this point.
            executeAppTransition(options);
            if (DEBUG_STATES) Slog.d(TAG_STATES,
                    "resumeTopActivityLocked: Top activity resumed " + next);
            if (DEBUG_STACK) mStackSupervisor.validateTopActivitiesLocked();
            return false;
        }

        // If we are sleeping, and there is no resumed activity, and the top
        // activity is paused, well that is the state we want.
        if (shouldSleepOrShutDownActivities()
                && mLastPausedActivity == next
                && mStackSupervisor.allPausedActivitiesComplete()) {
            // Make sure we have executed any pending transitions, since there
            // should be nothing left to do at this point.
            executeAppTransition(options);
            if (DEBUG_STATES) Slog.d(TAG_STATES,
                    "resumeTopActivityLocked: Going to sleep and all paused");
            if (DEBUG_STACK) mStackSupervisor.validateTopActivitiesLocked();
            return false;
        }

        // Make sure that the user who owns this activity is started.  If not,
        // we will just leave it as is because someone should be bringing
        // another user's activities to the top of the stack.
        if (!mService.mUserController.hasStartedUserState(next.userId)) {
            Slog.w(TAG, "Skipping resume of top activity " + next
                    + ": user " + next.userId + " is stopped");
            if (DEBUG_STACK) mStackSupervisor.validateTopActivitiesLocked();
            return false;
        }

        // The activity may be waiting for stop, but that is no longer
        // appropriate for it.
        mStackSupervisor.mStoppingActivities.remove(next);
        mStackSupervisor.mGoingToSleepActivities.remove(next);
        next.sleeping = false;
        mStackSupervisor.mActivitiesWaitingForVisibleActivity.remove(next);

        if (DEBUG_SWITCH) Slog.v(TAG_SWITCH, "Resuming " + next);

        // If we are currently pausing an activity, then don't do anything until that is done.
        if (!mStackSupervisor.allPausedActivitiesComplete()) {
            if (DEBUG_SWITCH || DEBUG_PAUSE || DEBUG_STATES) Slog.v(TAG_PAUSE,
                    "resumeTopActivityLocked: Skip resume: some activity pausing.");
            if (DEBUG_STACK) mStackSupervisor.validateTopActivitiesLocked();
            return false;
        }

        mStackSupervisor.setLaunchSource(next.info.applicationInfo.uid);

        boolean lastResumedCanPip = false;
        ActivityRecord lastResumed = null;
        final ActivityStack lastFocusedStack = mStackSupervisor.getLastStack();
        if (lastFocusedStack != null && lastFocusedStack != this) {
            // So, why aren't we using prev here??? See the param comment on the method. prev doesn't
            // represent the last resumed activity. However, the last focus stack does if it isn't null.
            lastResumed = lastFocusedStack.mResumedActivity;
            if (userLeaving && inMultiWindowMode() && lastFocusedStack.shouldBeVisible(next)) {
                // The user isn't leaving if this stack is the multi-window mode and the last
                // focused stack should still be visible.
                if(DEBUG_USER_LEAVING) Slog.i(TAG_USER_LEAVING, "Overriding userLeaving to false"
                        + " next=" + next + " lastResumed=" + lastResumed);
                userLeaving = false;
            }
            lastResumedCanPip = lastResumed != null && lastResumed.checkEnterPictureInPictureState(
                    "resumeTopActivity", userLeaving /* beforeStopping */);
        }
        // If the flag RESUME_WHILE_PAUSING is set, then continue to schedule the previous activity
        // to be paused, while at the same time resuming the new resume activity only if the
        // previous activity can't go into Pip since we want to give Pip activities a chance to
        // enter Pip before resuming the next activity.
        final boolean resumeWhilePausing = (next.info.flags & FLAG_RESUME_WHILE_PAUSING) != 0
                && !lastResumedCanPip;

        boolean pausing = mStackSupervisor.pauseBackStacks(userLeaving, next, false);
        if (mResumedActivity != null) {
            if (DEBUG_STATES) Slog.d(TAG_STATES,
                    "resumeTopActivityLocked: Pausing " + mResumedActivity);
            pausing |= startPausingLocked(userLeaving, false, next, false);
        }
        if (pausing && !resumeWhilePausing) {
            if (DEBUG_SWITCH || DEBUG_STATES) Slog.v(TAG_STATES,
                    "resumeTopActivityLocked: Skip resume: need to start pausing");
            // At this point we want to put the upcoming activity's process
            // at the top of the LRU list, since we know we will be needing it
            // very soon and it would be a waste to let it get killed if it
            // happens to be sitting towards the end.
            if (next.app != null && next.app.thread != null) {
                mService.updateLruProcessLocked(next.app, true, null);
            }
            if (DEBUG_STACK) mStackSupervisor.validateTopActivitiesLocked();
            if (lastResumed != null) {
                lastResumed.setWillCloseOrEnterPip(true);
            }
            return true;
        } else if (mResumedActivity == next && next.isState(RESUMED)
                && mStackSupervisor.allResumedActivitiesComplete()) {
            // It is possible for the activity to be resumed when we paused back stacks above if the
            // next activity doesn't have to wait for pause to complete.
            // So, nothing else to-do except:
            // Make sure we have executed any pending transitions, since there
            // should be nothing left to do at this point.
            executeAppTransition(options);
            if (DEBUG_STATES) Slog.d(TAG_STATES,
                    "resumeTopActivityLocked: Top activity resumed (dontWaitForPause) " + next);
            if (DEBUG_STACK) mStackSupervisor.validateTopActivitiesLocked();
            return true;
        }

        // If the most recent activity was noHistory but was only stopped rather
        // than stopped+finished because the device went to sleep, we need to make
        // sure to finish it as we're making a new activity topmost.
        if (shouldSleepActivities() && mLastNoHistoryActivity != null &&
                !mLastNoHistoryActivity.finishing) {
            if (DEBUG_STATES) Slog.d(TAG_STATES,
                    "no-history finish of " + mLastNoHistoryActivity + " on new resume");
            requestFinishActivityLocked(mLastNoHistoryActivity.appToken, Activity.RESULT_CANCELED,
                    null, "resume-no-history", false);
            mLastNoHistoryActivity = null;
        }

        if (prev != null && prev != next) {
            if (!mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(prev)
                    && next != null && !next.nowVisible) {
                mStackSupervisor.mActivitiesWaitingForVisibleActivity.add(prev);
                if (DEBUG_SWITCH) Slog.v(TAG_SWITCH,
                        "Resuming top, waiting visible to hide: " + prev);
            } else {
                // The next activity is already visible, so hide the previous
                // activity's windows right now so we can show the new one ASAP.
                // We only do this if the previous is finishing, which should mean
                // it is on top of the one being resumed so hiding it quickly
                // is good.  Otherwise, we want to do the normal route of allowing
                // the resumed activity to be shown so we can decide if the
                // previous should actually be hidden depending on whether the
                // new one is found to be full-screen or not.
                if (prev.finishing) {
                    prev.setVisibility(false);
                    if (DEBUG_SWITCH) Slog.v(TAG_SWITCH,
                            "Not waiting for visible to hide: " + prev + ", waitingVisible="
                            + mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(prev)
                            + ", nowVisible=" + next.nowVisible);
                } else {
                    if (DEBUG_SWITCH) Slog.v(TAG_SWITCH,
                            "Previous already visible but still waiting to hide: " + prev
                            + ", waitingVisible="
                            + mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(prev)
                            + ", nowVisible=" + next.nowVisible);
                }
            }
        }

        // Launching this app's activity, make sure the app is no longer
        // considered stopped.
        try {
            AppGlobals.getPackageManager().setPackageStoppedState(
                    next.packageName, false, next.userId); /* TODO: Verify if correct userid */
        } catch (RemoteException e1) {
        } catch (IllegalArgumentException e) {
            Slog.w(TAG, "Failed trying to unstop package "
                    + next.packageName + ": " + e);
        }

        // We are starting up the next activity, so tell the window manager
        // that the previous one will be hidden soon.  This way it can know
        // to ignore it when computing the desired screen orientation.
        boolean anim = true;
        if (prev != null) {
            if (prev.finishing) {
                if (DEBUG_TRANSITION) Slog.v(TAG_TRANSITION,
                        "Prepare close transition: prev=" + prev);
                if (mStackSupervisor.mNoAnimActivities.contains(prev)) {
                    anim = false;
                    mWindowManager.prepareAppTransition(TRANSIT_NONE, false);
                } else {
                    mWindowManager.prepareAppTransition(prev.getTask() == next.getTask()
                            ? TRANSIT_ACTIVITY_CLOSE
                            : TRANSIT_TASK_CLOSE, false);
                }
                prev.setVisibility(false);
            } else {
                if (DEBUG_TRANSITION) Slog.v(TAG_TRANSITION,
                        "Prepare open transition: prev=" + prev);
                if (mStackSupervisor.mNoAnimActivities.contains(next)) {
                    anim = false;
                    mWindowManager.prepareAppTransition(TRANSIT_NONE, false);
                } else {
                    mWindowManager.prepareAppTransition(prev.getTask() == next.getTask()
                            ? TRANSIT_ACTIVITY_OPEN
                            : next.mLaunchTaskBehind
                                    ? TRANSIT_TASK_OPEN_BEHIND
                                    : TRANSIT_TASK_OPEN, false);
                }
            }
        } else {
            if (DEBUG_TRANSITION) Slog.v(TAG_TRANSITION, "Prepare open transition: no previous");
            if (mStackSupervisor.mNoAnimActivities.contains(next)) {
                anim = false;
                mWindowManager.prepareAppTransition(TRANSIT_NONE, false);
            } else {
                mWindowManager.prepareAppTransition(TRANSIT_ACTIVITY_OPEN, false);
            }
        }

        if (anim) {
            next.applyOptionsLocked();
        } else {
            next.clearOptionsLocked();
        }

        mStackSupervisor.mNoAnimActivities.clear();

        ActivityStack lastStack = mStackSupervisor.getLastStack();
        if (next.app != null && next.app.thread != null) {
            if (DEBUG_SWITCH) Slog.v(TAG_SWITCH, "Resume running: " + next
                    + " stopped=" + next.stopped + " visible=" + next.visible);

            // If the previous activity is translucent, force a visibility update of
            // the next activity, so that it's added to WM's opening app list, and
            // transition animation can be set up properly.
            // For example, pressing Home button with a translucent activity in focus.
            // Launcher is already visible in this case. If we don't add it to opening
            // apps, maybeUpdateTransitToWallpaper() will fail to identify this as a
            // TRANSIT_WALLPAPER_OPEN animation, and run some funny animation.
            final boolean lastActivityTranslucent = lastStack != null
                    && (lastStack.inMultiWindowMode()
                    || (lastStack.mLastPausedActivity != null
                    && !lastStack.mLastPausedActivity.fullscreen));

            // The contained logic must be synchronized, since we are both changing the visibility
            // and updating the {@link Configuration}. {@link ActivityRecord#setVisibility} will
            // ultimately cause the client code to schedule a layout. Since layouts retrieve the
            // current {@link Configuration}, we must ensure that the below code updates it before
            // the layout can occur.
            synchronized(mWindowManager.getWindowManagerLock()) {
                // This activity is now becoming visible.
                if (!next.visible || next.stopped || lastActivityTranslucent) {
                    next.setVisibility(true);
                }

                // schedule launch ticks to collect information about slow apps.
                next.startLaunchTickingLocked();

                ActivityRecord lastResumedActivity =
                        lastStack == null ? null :lastStack.mResumedActivity;
                final ActivityState lastState = next.getState();

                mService.updateCpuStats();

                if (DEBUG_STATES) Slog.v(TAG_STATES, "Moving to RESUMED: " + next
                        + " (in existing)");

                next.setState(RESUMED, "resumeTopActivityInnerLocked");

                mService.updateLruProcessLocked(next.app, true, null);
                updateLRUListLocked(next);
                mService.updateOomAdjLocked();

                // Have the window manager re-evaluate the orientation of
                // the screen based on the new activity order.
                boolean notUpdated = true;

                if (mStackSupervisor.isFocusedStack(this)) {
                    // We have special rotation behavior when here is some active activity that
                    // requests specific orientation or Keyguard is locked. Make sure all activity
                    // visibilities are set correctly as well as the transition is updated if needed
                    // to get the correct rotation behavior. Otherwise the following call to update
                    // the orientation may cause incorrect configurations delivered to client as a
                    // result of invisible window resize.
                    // TODO: Remove this once visibilities are set correctly immediately when
                    // starting an activity.
                    notUpdated = !mStackSupervisor.ensureVisibilityAndConfig(next, mDisplayId,
                            true /* markFrozenIfConfigChanged */, false /* deferResume */);
                }

                if (notUpdated) {
                    // The configuration update wasn't able to keep the existing
                    // instance of the activity, and instead started a new one.
                    // We should be all done, but let's just make sure our activity
                    // is still at the top and schedule another run if something
                    // weird happened.
                    ActivityRecord nextNext = topRunningActivityLocked();
                    if (DEBUG_SWITCH || DEBUG_STATES) Slog.i(TAG_STATES,
                            "Activity config changed during resume: " + next
                                    + ", new next: " + nextNext);
                    if (nextNext != next) {
                        // Do over!
                        mStackSupervisor.scheduleResumeTopActivities();
                    }
                    if (!next.visible || next.stopped) {
                        next.setVisibility(true);
                    }
                    next.completeResumeLocked();
                    if (DEBUG_STACK) mStackSupervisor.validateTopActivitiesLocked();
                    return true;
                }

                try {
                    final ClientTransaction transaction = ClientTransaction.obtain(next.app.thread,
                            next.appToken);
                    // Deliver all pending results.
                    ArrayList<ResultInfo> a = next.results;
                    if (a != null) {
                        final int N = a.size();
                        if (!next.finishing && N > 0) {
                            if (DEBUG_RESULTS) Slog.v(TAG_RESULTS,
                                    "Delivering results to " + next + ": " + a);
                            transaction.addCallback(ActivityResultItem.obtain(a));
                        }
                    }

                    if (next.newIntents != null) {
                        transaction.addCallback(NewIntentItem.obtain(next.newIntents,
                                false /* andPause */));
                    }

                    // Well the app will no longer be stopped.
                    // Clear app token stopped state in window manager if needed.
                    next.notifyAppResumed(next.stopped);

                    EventLog.writeEvent(EventLogTags.AM_RESUME_ACTIVITY, next.userId,
                            System.identityHashCode(next), next.getTask().taskId,
                            next.shortComponentName);

                    next.sleeping = false;
                    mService.getAppWarningsLocked().onResumeActivity(next);
                    mService.showAskCompatModeDialogLocked(next);
                    next.app.pendingUiClean = true;
                    next.app.forceProcessStateUpTo(mService.mTopProcessState);
                    next.clearOptionsLocked();
                    transaction.setLifecycleStateRequest(
                            ResumeActivityItem.obtain(next.app.repProcState,
                                    mService.isNextTransitionForward()));
                    mService.getLifecycleManager().scheduleTransaction(transaction);

                    if (DEBUG_STATES) Slog.d(TAG_STATES, "resumeTopActivityLocked: Resumed "
                            + next);
                } catch (Exception e) {
                    // Whoops, need to restart this activity!
                    if (DEBUG_STATES) Slog.v(TAG_STATES, "Resume failed; resetting state to "
                            + lastState + ": " + next);
                    next.setState(lastState, "resumeTopActivityInnerLocked");

                    // lastResumedActivity being non-null implies there is a lastStack present.
                    if (lastResumedActivity != null) {
                        lastResumedActivity.setState(RESUMED, "resumeTopActivityInnerLocked");
                    }

                    Slog.i(TAG, "Restarting because process died: " + next);
                    if (!next.hasBeenLaunched) {
                        next.hasBeenLaunched = true;
                    } else  if (SHOW_APP_STARTING_PREVIEW && lastStack != null
                            && lastStack.isTopStackOnDisplay()) {
                        next.showStartingWindow(null /* prev */, false /* newTask */,
                                false /* taskSwitch */);
                    }
                    mStackSupervisor.startSpecificActivityLocked(next, true, false);
                    if (DEBUG_STACK) mStackSupervisor.validateTopActivitiesLocked();
                    return true;
                }
            }

            // From this point on, if something goes wrong there is no way
            // to recover the activity.
            try {
                next.completeResumeLocked();
            } catch (Exception e) {
                // If any exception gets thrown, toss away this
                // activity and try the next one.
                Slog.w(TAG, "Exception thrown during resume of " + next, e);
                requestFinishActivityLocked(next.appToken, Activity.RESULT_CANCELED, null,
                        "resume-exception", true);
                if (DEBUG_STACK) mStackSupervisor.validateTopActivitiesLocked();
                return true;
            }
        } else {
            // Whoops, need to restart this activity!
            if (!next.hasBeenLaunched) {
                next.hasBeenLaunched = true;
            } else {
                if (SHOW_APP_STARTING_PREVIEW) {
                    next.showStartingWindow(null /* prev */, false /* newTask */,
                            false /* taskSwich */);
                }
                if (DEBUG_SWITCH) Slog.v(TAG_SWITCH, "Restarting: " + next);
            }
            if (DEBUG_STATES) Slog.d(TAG_STATES, "resumeTopActivityLocked: Restarting " + next);
            mStackSupervisor.startSpecificActivityLocked(next, true, true);
        }

        if (DEBUG_STACK) mStackSupervisor.validateTopActivitiesLocked();
        return true;
    }
```

## ActivityStackSupervisor

### startSpecificActivityLocked(ActivityRecord,boolean,boolean)

3个参数。

如果当前是应用进程内Activity跳转，则`app != null`，执行`realStartActivityLocked()`；

如果是跨进程Activity跳转（桌面点击应用图标进入应用），执行`mService.startProcessLocked()`启动新的应用进程。

```java
    void startSpecificActivityLocked(ActivityRecord r,
            boolean andResume, boolean checkConfig) {
        // Is this activity's application already running?
        ProcessRecord app = mService.getProcessRecordLocked(r.processName,
                r.info.applicationInfo.uid, true);

        getLaunchTimeTracker().setLaunchTime(r);

        if (app != null && app.thread != null) {
            try {
                if ((r.info.flags&ActivityInfo.FLAG_MULTIPROCESS) == 0
                        || !"android".equals(r.info.packageName)) {
                    // Don't add this if it is a platform component that is marked
                    // to run in multiple processes, because this is actually
                    // part of the framework so doesn't make sense to track as a
                    // separate apk in the process.
                    app.addPackage(r.info.packageName, r.info.applicationInfo.longVersionCode,
                            mService.mProcessStats);
                }
                realStartActivityLocked(r, app, andResume, checkConfig);
                return;
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception when starting activity "
                        + r.intent.getComponent().flattenToShortString(), e);
            }

            // If a dead object exception was thrown -- fall through to
            // restart the application.
        }

        mService.startProcessLocked(r.processName, r.info.applicationInfo, true, 0,
                "activity", r.intent.getComponent(), false, false, true);
    }
```

### realStartActivityLocked(ActivityRecord,ProcessRecord,boolean,boolean)

4个参数。

```java
    final boolean realStartActivityLocked(ActivityRecord r, ProcessRecord app,
            boolean andResume, boolean checkConfig) throws RemoteException {

        if (!allPausedActivitiesComplete()) {
            // While there are activities pausing we skipping starting any new activities until
            // pauses are complete. NOTE: that we also do this for activities that are starting in
            // the paused state because they will first be resumed then paused on the client side.
            if (DEBUG_SWITCH || DEBUG_PAUSE || DEBUG_STATES) Slog.v(TAG_PAUSE,
                    "realStartActivityLocked: Skipping start of r=" + r
                    + " some activities pausing...");
            return false;
        }

        final TaskRecord task = r.getTask();
        final ActivityStack stack = task.getStack();

        beginDeferResume();

        try {
            r.startFreezingScreenLocked(app, 0);

            // schedule launch ticks to collect information about slow apps.
            r.startLaunchTickingLocked();

            r.setProcess(app);

            if (getKeyguardController().isKeyguardLocked()) {
                r.notifyUnknownVisibilityLaunched();
            }

            // Have the window manager re-evaluate the orientation of the screen based on the new
            // activity order.  Note that as a result of this, it can call back into the activity
            // manager with a new orientation.  We don't care about that, because the activity is
            // not currently running so we are just restarting it anyway.
            if (checkConfig) {
                // Deferring resume here because we're going to launch new activity shortly.
                // We don't want to perform a redundant launch of the same record while ensuring
                // configurations and trying to resume top activity of focused stack.
                ensureVisibilityAndConfig(r, r.getDisplayId(),
                        false /* markFrozenIfConfigChanged */, true /* deferResume */);
            }

            if (r.getStack().checkKeyguardVisibility(r, true /* shouldBeVisible */,
                    true /* isTop */)) {
                // We only set the visibility to true if the activity is allowed to be visible
                // based on
                // keyguard state. This avoids setting this into motion in window manager that is
                // later cancelled due to later calls to ensure visible activities that set
                // visibility back to false.
                r.setVisibility(true);
            }

            final int applicationInfoUid =
                    (r.info.applicationInfo != null) ? r.info.applicationInfo.uid : -1;
            if ((r.userId != app.userId) || (r.appInfo.uid != applicationInfoUid)) {
                Slog.wtf(TAG,
                        "User ID for activity changing for " + r
                                + " appInfo.uid=" + r.appInfo.uid
                                + " info.ai.uid=" + applicationInfoUid
                                + " old=" + r.app + " new=" + app);
            }

            app.waitingToKill = null;
            r.launchCount++;
            r.lastLaunchTime = SystemClock.uptimeMillis();

            if (DEBUG_ALL) Slog.v(TAG, "Launching: " + r);

            int idx = app.activities.indexOf(r);
            if (idx < 0) {
                app.activities.add(r);
            }
            mService.updateLruProcessLocked(app, true, null);
            mService.updateOomAdjLocked();

            final LockTaskController lockTaskController = mService.getLockTaskController();
            if (task.mLockTaskAuth == LOCK_TASK_AUTH_LAUNCHABLE
                    || task.mLockTaskAuth == LOCK_TASK_AUTH_LAUNCHABLE_PRIV
                    || (task.mLockTaskAuth == LOCK_TASK_AUTH_WHITELISTED
                            && lockTaskController.getLockTaskModeState()
                                    == LOCK_TASK_MODE_LOCKED)) {
                lockTaskController.startLockTaskMode(task, false, 0 /* blank UID */);
            }

            try {
                if (app.thread == null) {
                    throw new RemoteException();
                }
                List<ResultInfo> results = null;
                List<ReferrerIntent> newIntents = null;
                if (andResume) {
                    // We don't need to deliver new intents and/or set results if activity is going
                    // to pause immediately after launch.
                    results = r.results;
                    newIntents = r.newIntents;
                }
                if (DEBUG_SWITCH) Slog.v(TAG_SWITCH,
                        "Launching: " + r + " icicle=" + r.icicle + " with results=" + results
                                + " newIntents=" + newIntents + " andResume=" + andResume);
                EventLog.writeEvent(EventLogTags.AM_RESTART_ACTIVITY, r.userId,
                        System.identityHashCode(r), task.taskId, r.shortComponentName);
                if (r.isActivityTypeHome()) {
                    // Home process is the root process of the task.
                    mService.mHomeProcess = task.mActivities.get(0).app;
                }
                mService.notifyPackageUse(r.intent.getComponent().getPackageName(),
                        PackageManager.NOTIFY_PACKAGE_USE_ACTIVITY);
                r.sleeping = false;
                r.forceNewConfig = false;
                mService.getAppWarningsLocked().onStartActivity(r);
                mService.showAskCompatModeDialogLocked(r);
                r.compat = mService.compatibilityInfoForPackageLocked(r.info.applicationInfo);
                ProfilerInfo profilerInfo = null;
                if (mService.mProfileApp != null && mService.mProfileApp.equals(app.processName)) {
                    if (mService.mProfileProc == null || mService.mProfileProc == app) {
                        mService.mProfileProc = app;
                        ProfilerInfo profilerInfoSvc = mService.mProfilerInfo;
                        if (profilerInfoSvc != null && profilerInfoSvc.profileFile != null) {
                            if (profilerInfoSvc.profileFd != null) {
                                try {
                                    profilerInfoSvc.profileFd = profilerInfoSvc.profileFd.dup();
                                } catch (IOException e) {
                                    profilerInfoSvc.closeFd();
                                }
                            }

                            profilerInfo = new ProfilerInfo(profilerInfoSvc);
                        }
                    }
                }

                app.hasShownUi = true;
                app.pendingUiClean = true;
                app.forceProcessStateUpTo(mService.mTopProcessState);
                // Because we could be starting an Activity in the system process this may not go
                // across a Binder interface which would create a new Configuration. Consequently
                // we have to always create a new Configuration here.

                final MergedConfiguration mergedConfiguration = new MergedConfiguration(
                        mService.getGlobalConfiguration(), r.getMergedOverrideConfiguration());
                r.setLastReportedConfiguration(mergedConfiguration);

                logIfTransactionTooLarge(r.intent, r.icicle);


                // Create activity launch transaction.
                final ClientTransaction clientTransaction = ClientTransaction.obtain(app.thread,
                        r.appToken);
                clientTransaction.addCallback(LaunchActivityItem.obtain(new Intent(r.intent),
                        System.identityHashCode(r), r.info,
                        // TODO: Have this take the merged configuration instead of separate global
                        // and override configs.
                        mergedConfiguration.getGlobalConfiguration(),
                        mergedConfiguration.getOverrideConfiguration(), r.compat,
                        r.launchedFromPackage, task.voiceInteractor, app.repProcState, r.icicle,
                        r.persistentState, results, newIntents, mService.isNextTransitionForward(),
                        profilerInfo));

                // Set desired final state.
                final ActivityLifecycleItem lifecycleItem;
                if (andResume) {
                    lifecycleItem = ResumeActivityItem.obtain(mService.isNextTransitionForward());
                } else {
                    lifecycleItem = PauseActivityItem.obtain();
                }
                clientTransaction.setLifecycleStateRequest(lifecycleItem);

                // Schedule transaction.
                mService.getLifecycleManager().scheduleTransaction(clientTransaction);


                if ((app.info.privateFlags & ApplicationInfo.PRIVATE_FLAG_CANT_SAVE_STATE) != 0
                        && mService.mHasHeavyWeightFeature) {
                    // This may be a heavy-weight process!  Note that the package
                    // manager will ensure that only activity can run in the main
                    // process of the .apk, which is the only thing that will be
                    // considered heavy-weight.
                    if (app.processName.equals(app.info.packageName)) {
                        if (mService.mHeavyWeightProcess != null
                                && mService.mHeavyWeightProcess != app) {
                            Slog.w(TAG, "Starting new heavy weight process " + app
                                    + " when already running "
                                    + mService.mHeavyWeightProcess);
                        }
                        mService.mHeavyWeightProcess = app;
                        Message msg = mService.mHandler.obtainMessage(
                                ActivityManagerService.POST_HEAVY_NOTIFICATION_MSG);
                        msg.obj = r;
                        mService.mHandler.sendMessage(msg);
                    }
                }

            } catch (RemoteException e) {
                if (r.launchFailed) {
                    // This is the second time we failed -- finish activity
                    // and give up.
                    Slog.e(TAG, "Second failure launching "
                            + r.intent.getComponent().flattenToShortString()
                            + ", giving up", e);
                    mService.appDiedLocked(app);
                    stack.requestFinishActivityLocked(r.appToken, Activity.RESULT_CANCELED, null,
                            "2nd-crash", false);
                    return false;
                }

                // This is the first time we failed -- restart process and
                // retry.
                r.launchFailed = true;
                app.activities.remove(r);
                throw e;
            }
        } finally {
            endDeferResume();
        }

        r.launchFailed = false;
        if (stack.updateLRUListLocked(r)) {
            Slog.w(TAG, "Activity " + r + " being launched, but already in LRU list");
        }

        // TODO(lifecycler): Resume or pause requests are done as part of launch transaction,
        // so updating the state should be done accordingly.
        if (andResume && readyToResume()) {
            // As part of the process of launching, ActivityThread also performs
            // a resume.
            stack.minimalResumeActivityLocked(r);
        } else {
            // This activity is not starting in the resumed state... which should look like we asked
            // it to pause+stop (but remain visible), and it has done so and reported back the
            // current icicle and other state.
            if (DEBUG_STATES) Slog.v(TAG_STATES,
                    "Moving to PAUSED: " + r + " (starting in paused state)");
            r.setState(PAUSED, "realStartActivityLocked");
        }

        // Launch the new version setup screen if needed.  We do this -after-
        // launching the initial activity (that is, home), so that it can have
        // a chance to initialize itself while in the background, making the
        // switch back to it faster and look better.
        if (isFocusedStack(stack)) {
            mService.getActivityStartController().startSetupActivity();
        }

        // Update any services we are bound to that might care about whether
        // their client may have activities.
        if (r.app != null) {
            mService.mServices.updateServiceConnectionActivitiesLocked(r.app);
        }

        return true;
    }
```

## ClientLifecycleManager

### scheduleTransaction(ClientTransaction)

1个参数。

```java
	void scheduleTransaction(ClientTransaction transaction) throws RemoteException {
        final IApplicationThread client = transaction.getClient();
        transaction.schedule();
        if (!(client instanceof Binder)) {
            // If client is not an instance of Binder - it's a remote call and at this point it is
            // safe to recycle the object. All objects used for local calls will be recycled after
            // the transaction is executed on client in ActivityThread.
            transaction.recycle();
        }
    }
```

## ClientTransaction

### schedule()

无参数。

```java
    public void schedule() throws RemoteException {
        mClient.scheduleTransaction(this);
    }
```

## ApplicationThread

### scheduleTransaction(ClientTransaction)

1个参数

```java
@Override
public void scheduleTransaction(ClientTransaction transaction) throws RemoteException {
	ActivityThread.this.scheduleTransaction(transaction);
}
```

## ClientTransactionHandler

### scheduleTransaction(ClientTransaction)

1个参数。

```java
    void scheduleTransaction(ClientTransaction transaction) {
        transaction.preExecute(this);
        sendMessage(ActivityThread.H.EXECUTE_TRANSACTION, transaction);
    }
```

## ActivityThread

### sendMessage(int,Object)

2个参数。

```java
    void sendMessage(int what, Object obj) {
        sendMessage(what, obj, 0, 0, false);
    }

    private void sendMessage(int what, Object obj, int arg1) {
        sendMessage(what, obj, arg1, 0, false);
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2) {
        sendMessage(what, obj, arg1, arg2, false);
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2, boolean async) {
        if (DEBUG_MESSAGES) Slog.v(
            TAG, "SCHEDULE " + what + " " + mH.codeToString(what)
            + ": " + arg1 + " / " + obj);
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (async) {
            msg.setAsynchronous(true);
        }
        mH.sendMessage(msg);
    }
```

## H

### handleMessage(Message)

```java
case EXECUTE_TRANSACTION:
    final ClientTransaction transaction = (ClientTransaction) msg.obj;
    mTransactionExecutor.execute(transaction);
    if (isSystem()) {
        // Client transactions inside system process are recycled on the client side
        // instead of ClientLifecycleManager to avoid being cleared before this
        // message is handled.
        transaction.recycle();
    }
    // TODO(lifecycler): Recycle locally scheduled transactions.
    break;
```

## TransactionExecutor

### execute(ClientTransaction)

```java
    public void execute(ClientTransaction transaction) {
        final IBinder token = transaction.getActivityToken();
        log("Start resolving transaction for client: " + mTransactionHandler + ", token: " + token);

        executeCallbacks(transaction);

        executeLifecycleState(transaction);
        mPendingActions.clear();
        log("End resolving transaction");
    }
```

### executeCallbacks(ClientTransaction)

```java
    @VisibleForTesting
    public void executeCallbacks(ClientTransaction transaction) {
        final List<ClientTransactionItem> callbacks = transaction.getCallbacks();
        if (callbacks == null) {
            // No callbacks to execute, return early.
            return;
        }
        log("Resolving callbacks");

        final IBinder token = transaction.getActivityToken();
        ActivityClientRecord r = mTransactionHandler.getActivityClient(token);

        // In case when post-execution state of the last callback matches the final state requested
        // for the activity in this transaction, we won't do the last transition here and do it when
        // moving to final state instead (because it may contain additional parameters from server).
        final ActivityLifecycleItem finalStateRequest = transaction.getLifecycleStateRequest();
        final int finalState = finalStateRequest != null ? finalStateRequest.getTargetState()
                : UNDEFINED;
        // Index of the last callback that requests some post-execution state.
        final int lastCallbackRequestingState = lastCallbackRequestingState(transaction);

        final int size = callbacks.size();
        for (int i = 0; i < size; ++i) {
            final ClientTransactionItem item = callbacks.get(i);
            log("Resolving callback: " + item);
            final int postExecutionState = item.getPostExecutionState();
            final int closestPreExecutionState = mHelper.getClosestPreExecutionState(r,
                    item.getPostExecutionState());
            if (closestPreExecutionState != UNDEFINED) {
                cycleToPath(r, closestPreExecutionState);
            }

            item.execute(mTransactionHandler, token, mPendingActions);
            item.postExecute(mTransactionHandler, token, mPendingActions);
            if (r == null) {
                // Launch activity request will create an activity record.
                r = mTransactionHandler.getActivityClient(token);
            }

            if (postExecutionState != UNDEFINED && r != null) {
                // Skip the very last transition and perform it by explicit state request instead.
                final boolean shouldExcludeLastTransition =
                        i == lastCallbackRequestingState && finalState == postExecutionState;
                cycleToPath(r, postExecutionState, shouldExcludeLastTransition);
            }
        }
    }
```

## LaunchActivityItem

### execute(ClientTransactionHandler,IBinder,PendingTransactionActions)

3个参数。

```java
    @Override
    public void execute(ClientTransactionHandler client, IBinder token,
            PendingTransactionActions pendingActions) {
        Trace.traceBegin(TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
        ActivityClientRecord r = new ActivityClientRecord(token, mIntent, mIdent, mInfo,
                mOverrideConfig, mCompatInfo, mReferrer, mVoiceInteractor, mState, mPersistentState,
                mPendingResults, mPendingNewIntents, mIsForward,
                mProfilerInfo, client);
        client.handleLaunchActivity(r, pendingActions, null /* customIntent */);
        Trace.traceEnd(TRACE_TAG_ACTIVITY_MANAGER);
    }
```

## ActivityThread

### handleLaunchActivity(ActivityClientRecord,PendingTransactionActions,Intent)

3个参数。

```java
    @Override
    public Activity handleLaunchActivity(ActivityClientRecord r,
            PendingTransactionActions pendingActions, Intent customIntent) {
        // If we are getting ready to gc after going to the background, well
        // we are back active so skip it.
        unscheduleGcIdler();
        mSomeActivitiesChanged = true;

        if (r.profilerInfo != null) {
            mProfiler.setProfiler(r.profilerInfo);
            mProfiler.startProfiling();
        }

        // Make sure we are running with the most recent config.
        handleConfigurationChanged(null, null);

        if (localLOGV) Slog.v(
            TAG, "Handling launch of " + r);

        // Initialize before creating the activity
        if (!ThreadedRenderer.sRendererDisabled) {
            GraphicsEnvironment.earlyInitEGL();
        }
        WindowManagerGlobal.initialize();

        final Activity a = performLaunchActivity(r, customIntent);

        if (a != null) {
            r.createdConfig = new Configuration(mConfiguration);
            reportSizeConfigurations(r);
            if (!r.activity.mFinished && pendingActions != null) {
                pendingActions.setOldState(r.state);
                pendingActions.setRestoreInstanceState(true);
                pendingActions.setCallOnPostCreate(true);
            }
        } else {
            // If there was an error, for any reason, tell the activity manager to stop us.
            try {
                ActivityManager.getService()
                        .finishActivity(r.token, Activity.RESULT_CANCELED, null,
                                Activity.DONT_FINISH_TASK_WITH_ACTIVITY);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }

        return a;
    }
```

### performLaunchActivity(ActivityClientRecord, Intent)

2个参数。

```java
    /**  Core implementation of activity launch. */
    private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
        ActivityInfo aInfo = r.activityInfo;
        if (r.packageInfo == null) {
            r.packageInfo = getPackageInfo(aInfo.applicationInfo, r.compatInfo,
                    Context.CONTEXT_INCLUDE_CODE);
        }

        ComponentName component = r.intent.getComponent();
        if (component == null) {
            component = r.intent.resolveActivity(
                mInitialApplication.getPackageManager());
            r.intent.setComponent(component);
        }

        if (r.activityInfo.targetActivity != null) {
            component = new ComponentName(r.activityInfo.packageName,
                    r.activityInfo.targetActivity);
        }

        ContextImpl appContext = createBaseContextForActivity(r);
        Activity activity = null;
        try {
            java.lang.ClassLoader cl = appContext.getClassLoader();
            activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);
            StrictMode.incrementExpectedActivityCount(activity.getClass());
            r.intent.setExtrasClassLoader(cl);
            r.intent.prepareToEnterProcess();
            if (r.state != null) {
                r.state.setClassLoader(cl);
            }
        } catch (Exception e) {
            if (!mInstrumentation.onException(activity, e)) {
                throw new RuntimeException(
                    "Unable to instantiate activity " + component
                    + ": " + e.toString(), e);
            }
        }

        try {
            Application app = r.packageInfo.makeApplication(false, mInstrumentation);

            if (localLOGV) Slog.v(TAG, "Performing launch of " + r);
            if (localLOGV) Slog.v(
                    TAG, r + ": app=" + app
                    + ", appName=" + app.getPackageName()
                    + ", pkg=" + r.packageInfo.getPackageName()
                    + ", comp=" + r.intent.getComponent().toShortString()
                    + ", dir=" + r.packageInfo.getAppDir());

            if (activity != null) {
                CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
                Configuration config = new Configuration(mCompatConfiguration);
                if (r.overrideConfig != null) {
                    config.updateFrom(r.overrideConfig);
                }
                if (DEBUG_CONFIGURATION) Slog.v(TAG, "Launching activity "
                        + r.activityInfo.name + " with config " + config);
                Window window = null;
                if (r.mPendingRemoveWindow != null && r.mPreserveWindow) {
                    window = r.mPendingRemoveWindow;
                    r.mPendingRemoveWindow = null;
                    r.mPendingRemoveWindowManager = null;
                }
                appContext.setOuterContext(activity);
                activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window, r.configCallback);

                if (customIntent != null) {
                    activity.mIntent = customIntent;
                }
                r.lastNonConfigurationInstances = null;
                checkAndBlockForNetworkAccess();
                activity.mStartedActivity = false;
                int theme = r.activityInfo.getThemeResource();
                if (theme != 0) {
                    activity.setTheme(theme);
                }

                activity.mCalled = false;
                if (r.isPersistable()) {
                    mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
                } else {
                    mInstrumentation.callActivityOnCreate(activity, r.state);
                }
                if (!activity.mCalled) {
                    throw new SuperNotCalledException(
                        "Activity " + r.intent.getComponent().toShortString() +
                        " did not call through to super.onCreate()");
                }
                r.activity = activity;
            }
            r.setState(ON_CREATE);

            mActivities.put(r.token, r);

        } catch (SuperNotCalledException e) {
            throw e;

        } catch (Exception e) {
            if (!mInstrumentation.onException(activity, e)) {
                throw new RuntimeException(
                    "Unable to start activity " + component
                    + ": " + e.toString(), e);
            }
        }

        return activity;
    }
```

## Instrumentation

### callActivityOnCreate(Activity,Bundle)

2个参数。

```java 
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        prePerformCreate(activity);
        activity.performCreate(icicle);
        postPerformCreate(activity);
    }
```

## Activity

### performCreate(Bundle)

```java
    final void performCreate(Bundle icicle) {
        performCreate(icicle, null);
    }

    final void performCreate(Bundle icicle, PersistableBundle persistentState) {
        mCanEnterPictureInPicture = true;
        restoreHasCurrentPermissionRequest(icicle);
        if (persistentState != null) {
            onCreate(icicle, persistentState);
        } else {
            onCreate(icicle);
        }
        writeEventLog(LOG_AM_ON_CREATE_CALLED, "performCreate");
        mActivityTransitionState.readState(icicle);

        mVisibleFromClient = !mWindow.getWindowStyle().getBoolean(
                com.android.internal.R.styleable.Window_windowNoDisplay, false);
        mFragments.dispatchActivityCreated();
        mActivityTransitionState.setEnterActivityOptions(this, getActivityOptions());
    }
```

### onCreate(Bundle)

1个参数。

```java
    @MainThread
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (DEBUG_LIFECYCLE) Slog.v(TAG, "onCreate " + this + ": " + savedInstanceState);

        if (mLastNonConfigurationInstances != null) {
            mFragments.restoreLoaderNonConfig(mLastNonConfigurationInstances.loaders);
        }
        if (mActivityInfo.parentActivityName != null) {
            if (mActionBar == null) {
                mEnableDefaultActionBarUp = true;
            } else {
                mActionBar.setDefaultDisplayHomeAsUpEnabled(true);
            }
        }
        if (savedInstanceState != null) {
            mAutoFillResetNeeded = savedInstanceState.getBoolean(AUTOFILL_RESET_NEEDED, false);
            mLastAutofillId = savedInstanceState.getInt(LAST_AUTOFILL_ID,
                    View.LAST_APP_AUTOFILL_ID);

            if (mAutoFillResetNeeded) {
                getAutofillManager().onCreate(savedInstanceState);
            }

            Parcelable p = savedInstanceState.getParcelable(FRAGMENTS_TAG);
            mFragments.restoreAllState(p, mLastNonConfigurationInstances != null
                    ? mLastNonConfigurationInstances.fragments : null);
        }
        mFragments.dispatchCreate();
        getApplication().dispatchActivityCreated(this, savedInstanceState);
        if (mVoiceInteractor != null) {
            mVoiceInteractor.attachActivity(this);
        }
        mRestoredFromBundle = savedInstanceState != null;
        mCalled = true;
    }
```

## IActivityManager

IActivityManager为aidl文件，声明在`frameworks/base/core/java/android/app/IActivityManager.aidl`路径下，声明如下：

```java
/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app;

import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.app.ContentProviderHolder;
import android.app.GrantedUriPermission;
import android.app.IApplicationThread;
import android.app.IActivityController;
import android.app.IAppTask;
import android.app.IAssistDataReceiver;
import android.app.IInstrumentationWatcher;
import android.app.IProcessObserver;
import android.app.IServiceConnection;
import android.app.IStopUserCallback;
import android.app.ITaskStackListener;
import android.app.IUiAutomationConnection;
import android.app.IUidObserver;
import android.app.IUserSwitchObserver;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.ProfilerInfo;
import android.app.WaitResult;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ParceledListSlice;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.IProgressListener;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.os.WorkSource;
import android.service.voice.IVoiceInteractionSession;
import android.view.IRecentsAnimationRunner;
import android.view.RemoteAnimationDefinition;
import android.view.RemoteAnimationAdapter;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.IResultReceiver;
import com.android.internal.policy.IKeyguardDismissCallback;

import java.util.List;

/**
 * System private API for talking with the activity manager service.  This
 * provides calls from the application back to the activity manager.
 *
 * {@hide}
 */
interface IActivityManager {
    // WARNING: when these transactions are updated, check if they are any callers on the native
    // side. If so, make sure they are using the correct transaction ids and arguments.
    // If a transaction which will also be used on the native side is being inserted, add it to
    // below block of transactions.

    // Since these transactions are also called from native code, these must be kept in sync with
    // the ones in frameworks/native/libs/binder/include/binder/IActivityManager.h
    // =============== Beginning of transactions used on native side as well ======================
    ParcelFileDescriptor openContentUri(in String uriString);
    void registerUidObserver(in IUidObserver observer, int which, int cutpoint,
            String callingPackage);
    void unregisterUidObserver(in IUidObserver observer);
    boolean isUidActive(int uid, String callingPackage);
    // =============== End of transactions used on native side as well ============================

    // Special low-level communication with activity manager.
    void handleApplicationCrash(in IBinder app,
            in ApplicationErrorReport.ParcelableCrashInfo crashInfo);
    int startActivity(in IApplicationThread caller, in String callingPackage, in Intent intent,
            in String resolvedType, in IBinder resultTo, in String resultWho, int requestCode,
            int flags, in ProfilerInfo profilerInfo, in Bundle options);
    void unhandledBack();

    boolean finishActivity(in IBinder token, int code, in Intent data, int finishTask);
    Intent registerReceiver(in IApplicationThread caller, in String callerPackage,
            in IIntentReceiver receiver, in IntentFilter filter,
            in String requiredPermission, int userId, int flags);
    void unregisterReceiver(in IIntentReceiver receiver);
    int broadcastIntent(in IApplicationThread caller, in Intent intent,
            in String resolvedType, in IIntentReceiver resultTo, int resultCode,
            in String resultData, in Bundle map, in String[] requiredPermissions,
            int appOp, in Bundle options, boolean serialized, boolean sticky, int userId);
    void unbroadcastIntent(in IApplicationThread caller, in Intent intent, int userId);
    oneway void finishReceiver(in IBinder who, int resultCode, in String resultData, in Bundle map,
            boolean abortBroadcast, int flags);
    void attachApplication(in IApplicationThread app, long startSeq);
    oneway void activityIdle(in IBinder token, in Configuration config,
            in boolean stopProfiling);
    void activityPaused(in IBinder token);
    oneway void activityStopped(in IBinder token, in Bundle state,
            in PersistableBundle persistentState, in CharSequence description);
    String getCallingPackage(in IBinder token);
    ComponentName getCallingActivity(in IBinder token);
    List<ActivityManager.RunningTaskInfo> getTasks(int maxNum);
    List<ActivityManager.RunningTaskInfo> getFilteredTasks(int maxNum, int ignoreActivityType,
            int ignoreWindowingMode);
    void moveTaskToFront(int task, int flags, in Bundle options);
    void moveTaskBackwards(int task);
    int getTaskForActivity(in IBinder token, in boolean onlyRoot);
    ContentProviderHolder getContentProvider(in IApplicationThread caller,
            in String name, int userId, boolean stable);
    void publishContentProviders(in IApplicationThread caller,
            in List<ContentProviderHolder> providers);
    boolean refContentProvider(in IBinder connection, int stableDelta, int unstableDelta);
    void finishSubActivity(in IBinder token, in String resultWho, int requestCode);
    PendingIntent getRunningServiceControlPanel(in ComponentName service);
    ComponentName startService(in IApplicationThread caller, in Intent service,
            in String resolvedType, boolean requireForeground, in String callingPackage, int userId);
    int stopService(in IApplicationThread caller, in Intent service,
            in String resolvedType, int userId);
    int bindService(in IApplicationThread caller, in IBinder token, in Intent service,
            in String resolvedType, in IServiceConnection connection, int flags,
            in String callingPackage, int userId);
    boolean unbindService(in IServiceConnection connection);
    void publishService(in IBinder token, in Intent intent, in IBinder service);
    void activityResumed(in IBinder token);
    void setDebugApp(in String packageName, boolean waitForDebugger, boolean persistent);
    void setAgentApp(in String packageName, @nullable String agent);
    void setAlwaysFinish(boolean enabled);
    boolean startInstrumentation(in ComponentName className, in String profileFile,
            int flags, in Bundle arguments, in IInstrumentationWatcher watcher,
            in IUiAutomationConnection connection, int userId,
            in String abiOverride);
    void addInstrumentationResults(in IApplicationThread target, in Bundle results);
    void finishInstrumentation(in IApplicationThread target, int resultCode,
            in Bundle results);
    /**
     * @return A copy of global {@link Configuration}, contains general settings for the entire
     *         system. Corresponds to the configuration of the default display.
     * @throws RemoteException
     */
    Configuration getConfiguration();
    /**
     * Updates global configuration and applies changes to the entire system.
     * @param values Update values for global configuration. If null is passed it will request the
     *               Window Manager to compute new config for the default display.
     * @throws RemoteException
     * @return Returns true if the configuration was updated.
     */
    boolean updateConfiguration(in Configuration values);
    boolean stopServiceToken(in ComponentName className, in IBinder token, int startId);
    ComponentName getActivityClassForToken(in IBinder token);
    String getPackageForToken(in IBinder token);
    void setProcessLimit(int max);
    int getProcessLimit();
    int checkPermission(in String permission, int pid, int uid);
    int checkUriPermission(in Uri uri, int pid, int uid, int mode, int userId,
            in IBinder callerToken);
    void grantUriPermission(in IApplicationThread caller, in String targetPkg, in Uri uri,
            int mode, int userId);
    void revokeUriPermission(in IApplicationThread caller, in String targetPkg, in Uri uri,
            int mode, int userId);
    void setActivityController(in IActivityController watcher, boolean imAMonkey);
    void showWaitingForDebugger(in IApplicationThread who, boolean waiting);
    /*
     * This will deliver the specified signal to all the persistent processes. Currently only
     * SIGUSR1 is delivered. All others are ignored.
     */
    void signalPersistentProcesses(int signal);

    ParceledListSlice getRecentTasks(int maxNum, int flags, int userId);
    oneway void serviceDoneExecuting(in IBinder token, int type, int startId, int res);
    oneway void activityDestroyed(in IBinder token);
    IIntentSender getIntentSender(int type, in String packageName, in IBinder token,
            in String resultWho, int requestCode, in Intent[] intents, in String[] resolvedTypes,
            int flags, in Bundle options, int userId);
    void cancelIntentSender(in IIntentSender sender);
    String getPackageForIntentSender(in IIntentSender sender);
    void registerIntentSenderCancelListener(in IIntentSender sender, in IResultReceiver receiver);
    void unregisterIntentSenderCancelListener(in IIntentSender sender, in IResultReceiver receiver);
    void enterSafeMode();
    boolean startNextMatchingActivity(in IBinder callingActivity,
            in Intent intent, in Bundle options);
    void noteWakeupAlarm(in IIntentSender sender, in WorkSource workSource, int sourceUid,
            in String sourcePkg, in String tag);
    void removeContentProvider(in IBinder connection, boolean stable);
    void setRequestedOrientation(in IBinder token, int requestedOrientation);
    int getRequestedOrientation(in IBinder token);
    void unbindFinished(in IBinder token, in Intent service, boolean doRebind);
    void setProcessImportant(in IBinder token, int pid, boolean isForeground, String reason);
    void setServiceForeground(in ComponentName className, in IBinder token,
            int id, in Notification notification, int flags);
    boolean moveActivityTaskToBack(in IBinder token, boolean nonRoot);
    void getMemoryInfo(out ActivityManager.MemoryInfo outInfo);
    List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState();
    boolean clearApplicationUserData(in String packageName, boolean keepState,
            in IPackageDataObserver observer, int userId);
    void forceStopPackage(in String packageName, int userId);
    boolean killPids(in int[] pids, in String reason, boolean secure);
    List<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags);
    ActivityManager.TaskDescription getTaskDescription(int taskId);
    // Retrieve running application processes in the system
    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses();
    // Get device configuration
    ConfigurationInfo getDeviceConfigurationInfo();
    IBinder peekService(in Intent service, in String resolvedType, in String callingPackage);
    // Turn on/off profiling in a particular process.
    boolean profileControl(in String process, int userId, boolean start,
            in ProfilerInfo profilerInfo, int profileType);
    boolean shutdown(int timeout);
    void stopAppSwitches();
    void resumeAppSwitches();
    boolean bindBackupAgent(in String packageName, int backupRestoreMode, int userId);
    void backupAgentCreated(in String packageName, in IBinder agent);
    void unbindBackupAgent(in ApplicationInfo appInfo);
    int getUidForIntentSender(in IIntentSender sender);
    int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll,
            boolean requireFull, in String name, in String callerPackage);
    void addPackageDependency(in String packageName);
    void killApplication(in String pkg, int appId, int userId, in String reason);
    void closeSystemDialogs(in String reason);
    Debug.MemoryInfo[] getProcessMemoryInfo(in int[] pids);
    void killApplicationProcess(in String processName, int uid);
    int startActivityIntentSender(in IApplicationThread caller,
            in IIntentSender target, in IBinder whitelistToken, in Intent fillInIntent,
            in String resolvedType, in IBinder resultTo, in String resultWho, int requestCode,
            int flagsMask, int flagsValues, in Bundle options);
    void overridePendingTransition(in IBinder token, in String packageName,
            int enterAnim, int exitAnim);
    // Special low-level communication with activity manager.
    boolean handleApplicationWtf(in IBinder app, in String tag, boolean system,
            in ApplicationErrorReport.ParcelableCrashInfo crashInfo);
    void killBackgroundProcesses(in String packageName, int userId);
    boolean isUserAMonkey();
    WaitResult startActivityAndWait(in IApplicationThread caller, in String callingPackage,
            in Intent intent, in String resolvedType, in IBinder resultTo, in String resultWho,
            int requestCode, int flags, in ProfilerInfo profilerInfo, in Bundle options,
            int userId);
    boolean willActivityBeVisible(in IBinder token);
    int startActivityWithConfig(in IApplicationThread caller, in String callingPackage,
            in Intent intent, in String resolvedType, in IBinder resultTo, in String resultWho,
            int requestCode, int startFlags, in Configuration newConfig,
            in Bundle options, int userId);
    // Retrieve info of applications installed on external media that are currently
    // running.
    List<ApplicationInfo> getRunningExternalApplications();
    void finishHeavyWeightApp();
    // A StrictMode violation to be handled.  The violationMask is a
    // subset of the original StrictMode policy bitmask, with only the
    // bit violated and penalty bits to be executed by the
    // ActivityManagerService remaining set.
    void handleApplicationStrictModeViolation(in IBinder app, int violationMask,
            in StrictMode.ViolationInfo crashInfo);
    boolean isImmersive(in IBinder token);
    void setImmersive(in IBinder token, boolean immersive);
    boolean isTopActivityImmersive();
    void crashApplication(int uid, int initialPid, in String packageName, int userId, in String message);
    String getProviderMimeType(in Uri uri, int userId);
    IBinder newUriPermissionOwner(in String name);
    void grantUriPermissionFromOwner(in IBinder owner, int fromUid, in String targetPkg,
            in Uri uri, int mode, int sourceUserId, int targetUserId);
    void revokeUriPermissionFromOwner(in IBinder owner, in Uri uri, int mode, int userId);
    int checkGrantUriPermission(int callingUid, in String targetPkg, in Uri uri,
            int modeFlags, int userId);
    // Cause the specified process to dump the specified heap.
    boolean dumpHeap(in String process, int userId, boolean managed, boolean mallocInfo,
            boolean runGc, in String path, in ParcelFileDescriptor fd);
    int startActivities(in IApplicationThread caller, in String callingPackage,
            in Intent[] intents, in String[] resolvedTypes, in IBinder resultTo,
            in Bundle options, int userId);
    boolean isUserRunning(int userid, int flags);
    oneway void activitySlept(in IBinder token);
    int getFrontActivityScreenCompatMode();
    void setFrontActivityScreenCompatMode(int mode);
    int getPackageScreenCompatMode(in String packageName);
    void setPackageScreenCompatMode(in String packageName, int mode);
    boolean getPackageAskScreenCompat(in String packageName);
    void setPackageAskScreenCompat(in String packageName, boolean ask);
    boolean switchUser(int userid);
    void setFocusedTask(int taskId);
    boolean removeTask(int taskId);
    void registerProcessObserver(in IProcessObserver observer);
    void unregisterProcessObserver(in IProcessObserver observer);
    boolean isIntentSenderTargetedToPackage(in IIntentSender sender);
    void updatePersistentConfiguration(in Configuration values);
    long[] getProcessPss(in int[] pids);
    void showBootMessage(in CharSequence msg, boolean always);
    void killAllBackgroundProcesses();
    ContentProviderHolder getContentProviderExternal(in String name, int userId,
            in IBinder token);
    void removeContentProviderExternal(in String name, in IBinder token);
    // Get memory information about the calling process.
    void getMyMemoryState(out ActivityManager.RunningAppProcessInfo outInfo);
    boolean killProcessesBelowForeground(in String reason);
    UserInfo getCurrentUser();
    boolean shouldUpRecreateTask(in IBinder token, in String destAffinity);
    boolean navigateUpTo(in IBinder token, in Intent target, int resultCode,
            in Intent resultData);
    /**
     * Informs ActivityManagerService that the keyguard is showing.
     *
     * @param showingKeyguard True if the keyguard is showing, false otherwise.
     * @param showingAod True if AOD is showing, false otherwise.
     * @param secondaryDisplayShowing The displayId of the secondary display on which the keyguard
     *        is showing, or INVALID_DISPLAY if there is no such display. Only meaningful if
     *        showing is true.
     */
    void setLockScreenShown(boolean showingKeyguard, boolean showingAod,
            int secondaryDisplayShowing);
    boolean finishActivityAffinity(in IBinder token);
    // This is not public because you need to be very careful in how you
    // manage your activity to make sure it is always the uid you expect.
    int getLaunchedFromUid(in IBinder activityToken);
    void unstableProviderDied(in IBinder connection);
    boolean isIntentSenderAnActivity(in IIntentSender sender);
    boolean isIntentSenderAForegroundService(in IIntentSender sender);
    int startActivityAsUser(in IApplicationThread caller, in String callingPackage,
            in Intent intent, in String resolvedType, in IBinder resultTo, in String resultWho,
            int requestCode, int flags, in ProfilerInfo profilerInfo,
            in Bundle options, int userId);
    int stopUser(int userid, boolean force, in IStopUserCallback callback);
    void registerUserSwitchObserver(in IUserSwitchObserver observer, in String name);
    void unregisterUserSwitchObserver(in IUserSwitchObserver observer);
    int[] getRunningUserIds();

    // Deprecated - This method is only used by a few internal components and it will soon be
    // replaced by a proper bug report API (which will be restricted to a few, pre-defined apps).
    // No new code should be calling it.
    void requestBugReport(int bugreportType);

    /**
     *  Takes a telephony bug report and notifies the user with the title and description
     *  that are passed to this API as parameters
     *
     *  @param shareTitle should be a valid legible string less than 50 chars long
     *  @param shareDescription should be less than 91 bytes when encoded into UTF-8 format
     *
     *  @throws IllegalArgumentException if shareTitle or shareDescription is too big or if the
     *          paremeters cannot be encoding to an UTF-8 charset.
     */
    void requestTelephonyBugReport(in String shareTitle, in String shareDescription);

    /**
     *  Deprecated - This method is only used by Wifi, and it will soon be replaced by a proper
     *  bug report API.
     *
     *  Takes a minimal bugreport of Wifi-related state.
     *
     *  @param shareTitle should be a valid legible string less than 50 chars long
     *  @param shareDescription should be less than 91 bytes when encoded into UTF-8 format
     *
     *  @throws IllegalArgumentException if shareTitle or shareDescription is too big or if the
     *          parameters cannot be encoding to an UTF-8 charset.
     */
    void requestWifiBugReport(in String shareTitle, in String shareDescription);

    long inputDispatchingTimedOut(int pid, boolean aboveSystem, in String reason);
    void clearPendingBackup();
    Intent getIntentForIntentSender(in IIntentSender sender);
    Bundle getAssistContextExtras(int requestType);
    void reportAssistContextExtras(in IBinder token, in Bundle extras,
            in AssistStructure structure, in AssistContent content, in Uri referrer);
    // This is not public because you need to be very careful in how you
    // manage your activity to make sure it is always the uid you expect.
    String getLaunchedFromPackage(in IBinder activityToken);
    void killUid(int appId, int userId, in String reason);
    void setUserIsMonkey(boolean monkey);
    void hang(in IBinder who, boolean allowRestart);

    /**
     * Sets the windowing mode for a specific task. Only works on tasks of type
     * {@link WindowConfiguration#ACTIVITY_TYPE_STANDARD}
     * @param taskId The id of the task to set the windowing mode for.
     * @param windowingMode The windowing mode to set for the task.
     * @param toTop If the task should be moved to the top once the windowing mode changes.
     */
    void setTaskWindowingMode(int taskId, int windowingMode, boolean toTop);
    void moveTaskToStack(int taskId, int stackId, boolean toTop);
    /**
     * Resizes the input stack id to the given bounds.
     *
     * @param stackId Id of the stack to resize.
     * @param bounds Bounds to resize the stack to or {@code null} for fullscreen.
     * @param allowResizeInDockedMode True if the resize should be allowed when the docked stack is
     *                                active.
     * @param preserveWindows True if the windows of activities contained in the stack should be
     *                        preserved.
     * @param animate True if the stack resize should be animated.
     * @param animationDuration The duration of the resize animation in milliseconds or -1 if the
     *                          default animation duration should be used.
     * @throws RemoteException
     */
    void resizeStack(int stackId, in Rect bounds, boolean allowResizeInDockedMode,
            boolean preserveWindows, boolean animate, int animationDuration);
    List<ActivityManager.StackInfo> getAllStackInfos();
    void setFocusedStack(int stackId);
    ActivityManager.StackInfo getFocusedStackInfo();
    ActivityManager.StackInfo getStackInfo(int windowingMode, int activityType);
    boolean convertFromTranslucent(in IBinder token);
    boolean convertToTranslucent(in IBinder token, in Bundle options);
    void notifyActivityDrawn(in IBinder token);
    void reportActivityFullyDrawn(in IBinder token, boolean restoredFromBundle);
    void restart();
    void performIdleMaintenance();
    void takePersistableUriPermission(in Uri uri, int modeFlags, String toPackage, int userId);
    void releasePersistableUriPermission(in Uri uri, int modeFlags, String toPackage, int userId);
    ParceledListSlice getPersistedUriPermissions(in String packageName, boolean incoming);
    void appNotRespondingViaProvider(in IBinder connection);
    Rect getTaskBounds(int taskId);
    int getActivityDisplayId(in IBinder activityToken);
    boolean setProcessMemoryTrimLevel(in String process, int uid, int level);


    // Start of L transactions
    String getTagForIntentSender(in IIntentSender sender, in String prefix);
    boolean startUserInBackground(int userid);
    void startLockTaskModeByToken(in IBinder token);
    void stopLockTaskModeByToken(in IBinder token);
    boolean isInLockTaskMode();
    void setTaskDescription(in IBinder token, in ActivityManager.TaskDescription values);
    int startVoiceActivity(in String callingPackage, int callingPid, int callingUid,
            in Intent intent, in String resolvedType, in IVoiceInteractionSession session,
            in IVoiceInteractor interactor, int flags, in ProfilerInfo profilerInfo,
            in Bundle options, int userId);
    int startAssistantActivity(in String callingPackage, int callingPid, int callingUid,
            in Intent intent, in String resolvedType, in Bundle options, int userId);
    void startRecentsActivity(in Intent intent, in IAssistDataReceiver assistDataReceiver,
            in IRecentsAnimationRunner recentsAnimationRunner);
    void cancelRecentsAnimation(boolean restoreHomeStackPosition);
    int startActivityFromRecents(int taskId, in Bundle options);
    Bundle getActivityOptions(in IBinder token);
    List<IBinder> getAppTasks(in String callingPackage);
    void startSystemLockTaskMode(int taskId);
    void stopSystemLockTaskMode();
    void finishVoiceTask(in IVoiceInteractionSession session);
    boolean isTopOfTask(in IBinder token);
    void notifyLaunchTaskBehindComplete(in IBinder token);
    void notifyEnterAnimationComplete(in IBinder token);
    int startActivityAsCaller(in IApplicationThread caller, in String callingPackage,
            in Intent intent, in String resolvedType, in IBinder resultTo, in String resultWho,
            int requestCode, int flags, in ProfilerInfo profilerInfo, in Bundle options,
            boolean ignoreTargetSecurity, int userId);
    int addAppTask(in IBinder activityToken, in Intent intent,
            in ActivityManager.TaskDescription description, in Bitmap thumbnail);
    Point getAppTaskThumbnailSize();
    boolean releaseActivityInstance(in IBinder token);
    void releaseSomeActivities(in IApplicationThread app);
    void bootAnimationComplete();
    Bitmap getTaskDescriptionIcon(in String filename, int userId);
    boolean launchAssistIntent(in Intent intent, int requestType, in String hint, int userHandle,
            in Bundle args);
    void startInPlaceAnimationOnFrontMostApplication(in Bundle opts);
    int checkPermissionWithToken(in String permission, int pid, int uid,
            in IBinder callerToken);
    void registerTaskStackListener(in ITaskStackListener listener);
    void unregisterTaskStackListener(in ITaskStackListener listener);

    void notifyCleartextNetwork(int uid, in byte[] firstPacket);
    int createStackOnDisplay(int displayId);
    void setTaskResizeable(int taskId, int resizeableMode);
    boolean requestAssistContextExtras(int requestType, in IAssistDataReceiver receiver,
            in Bundle receiverExtras, in IBinder activityToken,
            boolean focused, boolean newSessionId);
    void resizeTask(int taskId, in Rect bounds, int resizeMode);
    int getLockTaskModeState();
    void setDumpHeapDebugLimit(in String processName, int uid, long maxMemSize,
            in String reportPackage);
    void dumpHeapFinished(in String path);
    void setVoiceKeepAwake(in IVoiceInteractionSession session, boolean keepAwake);
    void updateLockTaskPackages(int userId, in String[] packages);
    void noteAlarmStart(in IIntentSender sender, in WorkSource workSource, int sourceUid, in String tag);
    void noteAlarmFinish(in IIntentSender sender, in WorkSource workSource, int sourceUid, in String tag);
    int getPackageProcessState(in String packageName, in String callingPackage);
    oneway void showLockTaskEscapeMessage(in IBinder token);
    void updateDeviceOwner(in String packageName);
    /**
     * Notify the system that the keyguard is going away.
     *
     * @param flags See {@link android.view.WindowManagerPolicyConstants#KEYGUARD_GOING_AWAY_FLAG_TO_SHADE}
     *              etc.
     */
    void keyguardGoingAway(int flags);
    int getUidProcessState(int uid, in String callingPackage);
    boolean isAssistDataAllowedOnCurrentActivity();
    boolean showAssistFromActivity(in IBinder token, in Bundle args);
    boolean isRootVoiceInteraction(in IBinder token);


    // Start of N transactions
    // Start Binder transaction tracking for all applications.
    boolean startBinderTracking();
    // Stop Binder transaction tracking for all applications and dump trace data to the given file
    // descriptor.
    boolean stopBinderTrackingAndDump(in ParcelFileDescriptor fd);
    /**
     * Try to place task to provided position. The final position might be different depending on
     * current user and stacks state. The task will be moved to target stack if it's currently in
     * different stack.
     */
    void positionTaskInStack(int taskId, int stackId, int position);
    void exitFreeformMode(in IBinder token);
    void reportSizeConfigurations(in IBinder token, in int[] horizontalSizeConfiguration,
            in int[] verticalSizeConfigurations, in int[] smallestWidthConfigurations);
    boolean setTaskWindowingModeSplitScreenPrimary(int taskId, int createMode, boolean toTop,
            boolean animate, in Rect initialBounds, boolean showRecents);
    /**
     * Dismisses split-screen multi-window mode.
     * {@param toTop} If true the current primary split-screen stack will be placed or left on top.
     */
    void dismissSplitScreenMode(boolean toTop);
    /**
     * Dismisses PiP
     * @param animate True if the dismissal should be animated.
     * @param animationDuration The duration of the resize animation in milliseconds or -1 if the
     *                          default animation duration should be used.
     */
    void dismissPip(boolean animate, int animationDuration);
    void suppressResizeConfigChanges(boolean suppress);
    void moveTasksToFullscreenStack(int fromStackId, boolean onTop);
    boolean moveTopActivityToPinnedStack(int stackId, in Rect bounds);
    boolean isAppStartModeDisabled(int uid, in String packageName);
    boolean unlockUser(int userid, in byte[] token, in byte[] secret,
            in IProgressListener listener);
    boolean isInMultiWindowMode(in IBinder token);
    boolean isInPictureInPictureMode(in IBinder token);
    void killPackageDependents(in String packageName, int userId);
    boolean enterPictureInPictureMode(in IBinder token, in PictureInPictureParams params);
    void setPictureInPictureParams(in IBinder token, in PictureInPictureParams params);
    int getMaxNumPictureInPictureActions(in IBinder token);
    void activityRelaunched(in IBinder token);
    IBinder getUriPermissionOwnerForActivity(in IBinder activityToken);
    /**
     * Resizes the docked stack, and all other stacks as the result of the dock stack bounds change.
     *
     * @param dockedBounds The bounds for the docked stack.
     * @param tempDockedTaskBounds The temporary bounds for the tasks in the docked stack, which
     *                             might be different from the stack bounds to allow more
     *                             flexibility while resizing, or {@code null} if they should be the
     *                             same as the stack bounds.
     * @param tempDockedTaskInsetBounds The temporary bounds for the tasks to calculate the insets.
     *                                  When resizing, we usually "freeze" the layout of a task. To
     *                                  achieve that, we also need to "freeze" the insets, which
     *                                  gets achieved by changing task bounds but not bounds used
     *                                  to calculate the insets in this transient state
     * @param tempOtherTaskBounds The temporary bounds for the tasks in all other stacks, or
     *                            {@code null} if they should be the same as the stack bounds.
     * @param tempOtherTaskInsetBounds Like {@code tempDockedTaskInsetBounds}, but for the other
     *                                 stacks.
     * @throws RemoteException
     */
    void resizeDockedStack(in Rect dockedBounds, in Rect tempDockedTaskBounds,
            in Rect tempDockedTaskInsetBounds,
            in Rect tempOtherTaskBounds, in Rect tempOtherTaskInsetBounds);
    /**
     * Sets whether we are currently in an interactive split screen resize operation where we
     * are changing the docked stack size.
     */
    void setSplitScreenResizing(boolean resizing);
    int setVrMode(in IBinder token, boolean enabled, in ComponentName packageName);
    // Gets the URI permissions granted to an arbitrary package (or all packages if null)
    // NOTE: this is different from getPersistedUriPermissions(), which returns the URIs the package
    // granted to another packages (instead of those granted to it).
    ParceledListSlice getGrantedUriPermissions(in String packageName, int userId);
    // Clears the URI permissions granted to an arbitrary package.
    void clearGrantedUriPermissions(in String packageName, int userId);
    boolean isAppForeground(int uid);
    void startLocalVoiceInteraction(in IBinder token, in Bundle options);
    void stopLocalVoiceInteraction(in IBinder token);
    boolean supportsLocalVoiceInteraction();
    void notifyPinnedStackAnimationStarted();
    void notifyPinnedStackAnimationEnded();
    void removeStack(int stackId);
    /**
     * Removes stacks in the input windowing modes from the system if they are of activity type
     * ACTIVITY_TYPE_STANDARD or ACTIVITY_TYPE_UNDEFINED
     */
    void removeStacksInWindowingModes(in int[] windowingModes);
    /** Removes stack of the activity types from the system. */
    void removeStacksWithActivityTypes(in int[] activityTypes);
    void makePackageIdle(String packageName, int userId);
    int getMemoryTrimLevel();
    /**
     * Resizes the pinned stack.
     *
     * @param pinnedBounds The bounds for the pinned stack.
     * @param tempPinnedTaskBounds The temporary bounds for the tasks in the pinned stack, which
     *                             might be different from the stack bounds to allow more
     *                             flexibility while resizing, or {@code null} if they should be the
     *                             same as the stack bounds.
     */
    void resizePinnedStack(in Rect pinnedBounds, in Rect tempPinnedTaskBounds);
    boolean isVrModePackageEnabled(in ComponentName packageName);
    void notifyLockedProfile(int userId);
    void startConfirmDeviceCredentialIntent(in Intent intent, in Bundle options);
    void sendIdleJobTrigger();
    int sendIntentSender(in IIntentSender target, in IBinder whitelistToken, int code,
            in Intent intent, in String resolvedType, in IIntentReceiver finishedReceiver,
            in String requiredPermission, in Bundle options);
    boolean isBackgroundRestricted(in String packageName);

    // Start of N MR1 transactions
    void setVrThread(int tid);
    void setRenderThread(int tid);
    /**
     * Lets activity manager know whether the calling process is currently showing "top-level" UI
     * that is not an activity, i.e. windows on the screen the user is currently interacting with.
     *
     * <p>This flag can only be set for persistent processes.
     *
     * @param hasTopUi Whether the calling process has "top-level" UI.
     */
    void setHasTopUi(boolean hasTopUi);

    // Start of O transactions
    /**
     * Updates override configuration applied to specific display.
     * @param values Update values for display configuration. If null is passed it will request the
     *               Window Manager to compute new config for the specified display.
     * @param displayId Id of the display to apply the config to.
     * @throws RemoteException
     * @return Returns true if the configuration was updated.
     */
    boolean updateDisplayOverrideConfiguration(in Configuration values, int displayId);
    void moveStackToDisplay(int stackId, int displayId);
    boolean requestAutofillData(in IAssistDataReceiver receiver, in Bundle receiverExtras,
            in IBinder activityToken, int flags);
    void dismissKeyguard(in IBinder token, in IKeyguardDismissCallback callback,
            in CharSequence message);
    int restartUserInBackground(int userId);

    /** Cancels the window transitions for the given task. */
    void cancelTaskWindowTransition(int taskId);

    /**
     * @param taskId the id of the task to retrieve the sAutoapshots for
     * @param reducedResolution if set, if the snapshot needs to be loaded from disk, this will load
     *                          a reduced resolution of it, which is much faster
     * @return a graphic buffer representing a screenshot of a task
     */
    ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution);

    void scheduleApplicationInfoChanged(in List<String> packageNames, int userId);
    void setPersistentVrThread(int tid);

    void waitForNetworkStateUpdate(long procStateSeq);

    /**
     * See {@link android.app.Activity#setDisablePreviewScreenshots}
     */
    void setDisablePreviewScreenshots(IBinder token, boolean disable);

    /**
     * Return the user id of last resumed activity.
     */
    int getLastResumedActivityUserId();

    /**
     * Add a bare uid to the background restrictions whitelist.  Only the system uid may call this.
     */
    void backgroundWhitelistUid(int uid);

    // Start of P transactions
    void updateLockTaskFeatures(int userId, int flags);

    // WARNING: when these transactions are updated, check if they are any callers on the native
    // side. If so, make sure they are using the correct transaction ids and arguments.
    // If a transaction which will also be used on the native side is being inserted, add it
    // alongside with other transactions of this kind at the top of this file.

    void setShowWhenLocked(in IBinder token, boolean showWhenLocked);
    void setTurnScreenOn(in IBinder token, boolean turnScreenOn);

    /**
     *  Similar to {@link #startUserInBackground(int userId), but with a listener to report
     *  user unlock progress.
     */
    boolean startUserInBackgroundWithListener(int userid, IProgressListener unlockProgressListener);

    /**
     * Registers remote animations for a specific activity.
     */
    void registerRemoteAnimations(in IBinder token, in RemoteAnimationDefinition definition);

    /**
     * Registers a remote animation to be run for all activity starts from a certain package during
     * a short predefined amount of time.
     */
    void registerRemoteAnimationForNextActivityStart(in String packageName,
           in RemoteAnimationAdapter adapter);

    /** @see android.app.ActivityManager#alwaysShowUnsupportedCompileSdkWarning */
    void alwaysShowUnsupportedCompileSdkWarning(in ComponentName activity);
}
```

