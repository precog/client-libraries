package io.precog.api.dto;

import java.util.Map;

/**
 * User Account info
 * <p/>
 * User: gabriel
 * Date: 10/26/12
 */
public class AccountInfo {
    private String accountId;
    private String email;
    private int accountCreationDate;
    private String apiKey;
    private String rootPath;
    private Map<String, String> plan;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAccountCreationDate() {
        return accountCreationDate;
    }

    public void setAccountCreationDate(int accountCreationDate) {
        this.accountCreationDate = accountCreationDate;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public Map<String, String> getPlan() {
        return plan;
    }

    public void setPlan(Map<String, String> plan) {
        this.plan = plan;
    }
}
