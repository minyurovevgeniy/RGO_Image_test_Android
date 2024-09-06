package ru.uspu.rgo_image_test;

public class Test {
    public String testToken;
    public String testId;
    public String testTitle;
    public String testDescription;

    public Test()
    {

    }

    public Test(String _testToken, String _TestId, String _testTitle, String _testDescription)
    {
        testToken=_testToken;
        testId=_TestId;
        testTitle=_testTitle;
        testDescription=_testDescription;
    }

    public String getTestId()
    {
        return testId;
    }

    public String getTestToken()
    {
        return testToken;
    }

    public String getTestTitle() {
        return testTitle;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestId(String testId)
    {
        this.testId = testId;
    }

    public void setTestToken(String testToken)
    {
        this.testToken = testToken;
    }

    public void setTestTitle(String testTitle) {
        this.testTitle = testTitle;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }
}
