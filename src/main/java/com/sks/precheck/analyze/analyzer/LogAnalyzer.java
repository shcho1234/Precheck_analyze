package com.sks.precheck.analyze.analyzer;

import com.sks.precheck.analyze.domain.AnalyzeResult;
import com.sks.precheck.analyze.domain.CollectLog;
import com.sks.precheck.analyze.domain.policy.AnalyzePolicy;

public interface LogAnalyzer {

    AnalyzeResult analyze(CollectLog log, AnalyzePolicy policy);
}
