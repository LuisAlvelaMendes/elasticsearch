/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.security;

import org.elasticsearch.bootstrap.FIPSContext;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.core.XPackSettings;
import org.elasticsearch.xpack.security.FIPSInterface.FIPSCheckResult;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;

public class PasswordHashingAlgorithmFIPSCheckTests extends ESTestCase {

    public void testPBKDF2AlgorithmIsAllowed() {
        {
            final Settings settings = Settings.builder()
                    .put(XPackSettings.FIPS_MODE_ENABLED.getKey(), true)
                    .put(XPackSettings.PASSWORD_HASHING_ALGORITHM.getKey(), "PBKDF2_10000")
                    .build();
            final FIPSCheckResult result =
                    new FIPSChecks().passwordHashingAlgorithmCheck(new FIPSContext(settings));
            assertFalse(result.isFailure());
        }

        {
            final Settings settings = Settings.builder()
                    .put(XPackSettings.FIPS_MODE_ENABLED.getKey(), true)
                    .put(XPackSettings.PASSWORD_HASHING_ALGORITHM.getKey(), "PBKDF2")
                    .build();
            final FIPSCheckResult result =
            		new FIPSChecks().passwordHashingAlgorithmCheck(new FIPSContext(settings));
            assertFalse(result.isFailure());
        }
    }

    public void testBCRYPTAlgorithmDependsOnFipsMode() {
        for (final Boolean fipsModeEnabled : Arrays.asList(true, false)) {
            for (final String passwordHashingAlgorithm : Arrays.asList(null, "BCRYPT", "BCRYPT11")) {
                runBCRYPTTest(fipsModeEnabled, passwordHashingAlgorithm);
            }
        }
    }

    private void runBCRYPTTest(final boolean fipsModeEnabled, final String passwordHashingAlgorithm) {
        final Settings.Builder builder = Settings.builder().put(XPackSettings.FIPS_MODE_ENABLED.getKey(), fipsModeEnabled);
        if (passwordHashingAlgorithm != null) {
            builder.put(XPackSettings.PASSWORD_HASHING_ALGORITHM.getKey(), passwordHashingAlgorithm);
        }
        final Settings settings = builder.build();
        final FIPSCheckResult result =
        		new FIPSChecks().passwordHashingAlgorithmCheck(new FIPSContext(settings));
        assertThat(result.isFailure(), equalTo(fipsModeEnabled));
    }

}
