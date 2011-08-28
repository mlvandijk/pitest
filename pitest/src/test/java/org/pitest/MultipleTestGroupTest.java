/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.functional.Option;
import org.pitest.internal.IsolationUtils;

public class MultipleTestGroupTest {

  @Mock
  private TestUnit          emptyTestUnit;
  @Mock
  private TestUnit          emptyTestUnit2;

  @Mock
  private ResultCollector   rc;

  private MultipleTestGroup testee;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    when(this.emptyTestUnit.getDescription()).thenReturn(
        new Description("foo", String.class, null));
    when(this.emptyTestUnit2.getDescription()).thenReturn(
        new Description("foo2", String.class, null));
  }

  @Test
  public void shouldIterateOverChildTestUnits() {
    this.testee = new MultipleTestGroup(
        Collections.singletonList(this.emptyTestUnit));
    assertSame(this.emptyTestUnit, this.testee.iterator().next());
  }

  @Test
  public void shouldReturnGroupContainingChildrenMatchingFilter() {
    when(this.emptyTestUnit2.filter(any(TestFilter.class))).thenReturn(
        Option.some(this.emptyTestUnit2));
    when(this.emptyTestUnit.filter(any(TestFilter.class))).thenReturn(
        Option.<TestUnit> none());

    this.testee = new MultipleTestGroup(Arrays.asList(this.emptyTestUnit,
        this.emptyTestUnit2));
    final Option<TestUnit> actual = this.testee.filter(irrelevant());
    assertEquals(
        Option.some(new MultipleTestGroup(Arrays.asList(this.emptyTestUnit2))),
        actual);
  }

  @Test
  public void shouldReturnNoneWhenNoChildrenMatchFilter() {
    when(this.emptyTestUnit.filter(any(TestFilter.class))).thenReturn(
        Option.<TestUnit> none());

    this.testee = new MultipleTestGroup(Arrays.asList(this.emptyTestUnit));
    final Option<TestUnit> actual = this.testee.filter(irrelevant());
    assertEquals(Option.none(), actual);
  }

  private TestFilter irrelevant() {
    return null;
  }

  @Test
  public void shouldExecuteAllChildren() {
    this.testee = new MultipleTestGroup(Arrays.asList(this.emptyTestUnit,
        this.emptyTestUnit2));
    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.emptyTestUnit, times(1)).execute(
        IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.emptyTestUnit2, times(1)).execute(
        IsolationUtils.getContextClassLoader(), this.rc);
  }

  @Test
  public void shouldStopExecutingWhenResultCollectorRequestsStope() {
    when(this.rc.shouldExit()).thenReturn(true);
    this.testee = new MultipleTestGroup(Arrays.asList(this.emptyTestUnit,
        this.emptyTestUnit2));
    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.emptyTestUnit, times(1)).execute(
        IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.emptyTestUnit2, never()).execute(
        IsolationUtils.getContextClassLoader(), this.rc);
  }

}