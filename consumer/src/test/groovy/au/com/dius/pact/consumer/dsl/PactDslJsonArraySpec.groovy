package au.com.dius.pact.consumer.dsl

import au.com.dius.pact.core.model.PactSpecVersion
import au.com.dius.pact.core.model.matchingrules.MatchingRuleGroup
import au.com.dius.pact.core.model.matchingrules.NumberTypeMatcher
import au.com.dius.pact.core.model.matchingrules.RegexMatcher
import au.com.dius.pact.core.model.matchingrules.RuleLogic
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

class PactDslJsonArraySpec extends Specification {

  def 'close must close off all parents and return the root'() {
    given:
      def root = new PactDslJsonArray()
      def obj = new PactDslJsonBody('b', '', root)
      def array = new PactDslJsonArray('c', '', obj)

    when:
      def result = array.close()

    then:
      root.closed
      obj.closed
      array.closed
      result.is root
  }

  def 'min array like function should set the example size to the min size'() {
    expect:
    obj.close().body.get(0).size() == 2

    where:
    obj = new PactDslJsonArray().minArrayLike(2).id()
  }

  def 'min array like function should validate the number of examples match the min size'() {
    when:
    new PactDslJsonArray().minArrayLike(3, 2)

    then:
    thrown(IllegalArgumentException)
  }

  def 'max array like function should validate the number of examples match the max size'() {
    when:
    new PactDslJsonArray().maxArrayLike(3, 4)

    then:
    thrown(IllegalArgumentException)
  }

  def 'minMax array like function should validate the min and max size'() {
    when:
    new PactDslJsonArray().minMaxArrayLike(3, 2)

    then:
    thrown(IllegalArgumentException)
  }

  def 'minMax array like function should validate the number of examples match the min size'() {
    when:
    new PactDslJsonArray().minMaxArrayLike(2, 3, 1)

    then:
    thrown(IllegalArgumentException)
  }

  def 'minMax array like function should validate the number of examples match the max size'() {
    when:
    new PactDslJsonArray().minMaxArrayLike(2, 3, 4)

    then:
    thrown(IllegalArgumentException)
  }

  def 'static min array like function should validate the number of examples match the min size'() {
    when:
    PactDslJsonArray.arrayMinLike(3, 2)

    then:
    thrown(IllegalArgumentException)
  }

  def 'static max array like function should validate the number of examples match the max size'() {
    when:
    PactDslJsonArray.arrayMaxLike(3, 4)

    then:
    thrown(IllegalArgumentException)
  }

  def 'static minmax array like function should validate the number of examples match the max size'() {
    when:
    PactDslJsonArray.arrayMinMaxLike(2, 3, 4)

    then:
    thrown(IllegalArgumentException)
  }

  def 'static minmax array like function should validate the number of examples match the min size'() {
    when:
    PactDslJsonArray.arrayMinMaxLike(2, 3, 1)

    then:
    thrown(IllegalArgumentException)
  }

  def 'static minmax array like function should validate the min and max size'() {
    when:
    PactDslJsonArray.arrayMinMaxLike(4, 3)

    then:
    thrown(IllegalArgumentException)
  }

  def 'each array with max like function should validate the number of examples match the max size'() {
    when:
    new PactDslJsonArray().eachArrayWithMaxLike(4, 3)

    then:
    thrown(IllegalArgumentException)
  }

  def 'each array with min function should validate the number of examples match the min size'() {
    when:
    new PactDslJsonArray().eachArrayWithMinLike(2, 3)

    then:
    thrown(IllegalArgumentException)
  }

  def 'each array with min and max like function should validate the number of examples match the max size'() {
    when:
    new PactDslJsonArray().eachArrayWithMinMaxLike(5, 3, 4)

    then:
    thrown(IllegalArgumentException)
  }

  def 'each array with min and max like function should validate the number of examples match the min size'() {
    when:
    new PactDslJsonArray().eachArrayWithMinMaxLike(1, 3, 4)

    then:
    thrown(IllegalArgumentException)
  }

  def 'each array with min and max like function should validate the min and max size'() {
    when:
    new PactDslJsonArray().eachArrayWithMinMaxLike(4, 3)

    then:
    thrown(IllegalArgumentException)
  }

  def 'with nested objects, the rule logic value should be copied'() {
    expect:
    body.matchers.matchingRules['[0][*].foo.bar'].ruleLogic == RuleLogic.OR

    where:
    body = new PactDslJsonArray()
      .eachLike()
        .object('foo')
          .or('bar', 42, PM.numberType(), PM.nullValue())
        .closeObject()
      .closeObject()
      .closeArray()
  }

  @Unroll
  def 'The #function functions should auto-close the inner object'() {
    expect:
    obj.closeArray() is body
    obj.closed
    !body.closed
    array.closed

    where:

    function << ['eachLike', 'minArrayLike', 'maxArrayLike']
    args << [['myArr'], ['myArr', 1], ['myArr', 1]]

    body = new PactDslJsonBody()
    obj = body."$function"(*args)
        .stringType('myString2')
        .object('myArrSubObj')
          .stringType('myString3')
        .closeObject()
    array = obj.parent
  }

  @Issue('#628')
  def 'test for behaviour of close for issue 628'() {
    given:
    def body = new PactDslJsonArray()
    body
      .object()
      .stringType('messageId', 'test')
      .stringType('date', 'test')
      .stringType('contractVersion', 'test')
      .closeObject()
      .object()
      .stringType('name', 'srm.countries.get')
      .stringType('iri', 'some_iri')
      .closeObject()
      .closeArray()

    expect:
    body.close().matchers.toMap(PactSpecVersion.V2) == [
      '$.body[0].messageId': [match: 'type'],
      '$.body[0].date': [match: 'type'],
      '$.body[0].contractVersion': [match: 'type'],
      '$.body[1].name': [match: 'type'],
      '$.body[1].iri': [match: 'type']
    ]
  }

  def 'support for date and time expressions'() {
    given:
    PactDslJsonArray body = new PactDslJsonArray()
    body.dateExpression('today + 1 day')
      .timeExpression('now + 1 hour')
      .datetimeExpression('today + 1 hour')
      .closeArray()

    expect:
    body.matchers.toMap(PactSpecVersion.V3) == [
      '$[0]': [matchers: [[match: 'date', date: 'yyyy-MM-dd']], combine: 'AND'],
      '$[1]': [matchers: [[match: 'time', time: 'HH:mm:ss']], combine: 'AND'],
      '$[2]': [matchers: [[match: 'timestamp', timestamp: "yyyy-MM-dd'T'HH:mm:ss"]], combine: 'AND']]

    body.generators.toMap(PactSpecVersion.V3) == [body: [
      '$[0]': [type: 'Date', format: 'yyyy-MM-dd', expression: 'today + 1 day'],
      '$[1]': [type: 'Time', format: 'HH:mm:ss', expression: 'now + 1 hour'],
      '$[2]': [type: 'DateTime', format: "yyyy-MM-dd'T'HH:mm:ss", expression: 'today + 1 hour']]]
  }

  def 'unordered array with min and max function should validate the minSize less than maxSize'() {
    when:
    new PactDslJsonArray().unorderedMinMaxArray(4, 3)

    then:
    thrown(IllegalArgumentException)
  }

  def 'each like with DSLPart'() {
    given:
    PactDslJsonArray body = new PactDslJsonArray()
      .eachLike()
      .stringType('messageId', 'test')
      .stringType('date', 'test')
      .stringType('contractVersion', 'test')
      .closeArray()
    PactDslJsonBody message = new PactDslJsonBody()
      .stringType('messageId', 'test')
      .stringType('date', 'test')
      .stringType('contractVersion', 'test')
    PactDslJsonArray body2 = new PactDslJsonArray()
      .eachLike(message)

    expect:
    body.body.toString() == body2.body.toString()
    body.matchers == body2.matchers
  }

  def 'min like with DSLPart'() {
    given:
    PactDslJsonArray body = new PactDslJsonArray()
      .minArrayLike(1)
      .stringType('messageId', 'test')
      .stringType('date', 'test')
      .stringType('contractVersion', 'test')
      .closeArray()
    PactDslJsonBody message = new PactDslJsonBody()
      .stringType('messageId', 'test')
      .stringType('date', 'test')
      .stringType('contractVersion', 'test')
    PactDslJsonArray body2 = new PactDslJsonArray()
      .minArrayLike(1, message)

    expect:
    body.body.toString() == body2.body.toString()
    body.matchers == body2.matchers
  }

  def 'max like with DSLPart'() {
    given:
    PactDslJsonArray body = new PactDslJsonArray()
      .maxArrayLike(10)
      .stringType('messageId', 'test')
      .stringType('date', 'test')
      .stringType('contractVersion', 'test')
      .closeArray()
    PactDslJsonBody message = new PactDslJsonBody()
      .stringType('messageId', 'test')
      .stringType('date', 'test')
      .stringType('contractVersion', 'test')
    PactDslJsonArray body2 = new PactDslJsonArray()
      .maxArrayLike(10, message)

    expect:
    body.body.toString() == body2.body.toString()
    body.matchers == body2.matchers
  }

  def 'like matcher'() {
    given:
    PactDslJsonArray body = new PactDslJsonArray().like('Test').like(100)

    expect:
    body.body.toString() == '["Test",100]'
    body.matchers.toMap(PactSpecVersion.V3) == [
      '[0]': [matchers: [[match: 'type']], combine: 'AND'],
      '[1]': [matchers: [[match: 'type']], combine: 'AND']
    ]
  }

  @Issue('1600')
  def 'Match number type with Regex'() {
    when:
    PactDslJsonArray body = new PactDslJsonArray()
      .numberMatching('\\d+\\.\\d{2}', 2.01)
      .decimalMatching('\\d+\\.\\d{2}', 2.01)
      .integerMatching('\\d{5}', 90210)
      .close()

    then:
    body.toString() == '[2.01,2.01,90210]'
    body.matchers.matchingRules['$[0]'] == new MatchingRuleGroup([new NumberTypeMatcher(NumberTypeMatcher.NumberType.NUMBER), new RegexMatcher('\\d+\\.\\d{2}', '2.01')])
    body.matchers.matchingRules['$[1]'] == new MatchingRuleGroup([new NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL), new RegexMatcher('\\d+\\.\\d{2}', '2.01')])
    body.matchers.matchingRules['$[2]'] == new MatchingRuleGroup([new NumberTypeMatcher(NumberTypeMatcher.NumberType.INTEGER), new RegexMatcher('\\d{5}', '90210')])
  }
}
