package com.huangmb.jenkins.wechat;

import com.huangmb.jenkins.wechat.bean.Chat;
import com.huangmb.jenkins.wechat.bean.CustomGroup;
import com.huangmb.jenkins.wechat.bean.WechatDepartment;
import com.huangmb.jenkins.wechat.bean.WechatTag;
import com.huangmb.jenkins.wechat.bean.WechatUser;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import org.jenkinsci.Symbol;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Extension
@Symbol("weworkchatknotify")
public class WechatNotifierDescriptor extends BuildStepDescriptor<Publisher> {

    public WechatNotifierDescriptor() {
        super(WechatNotifier.class);
        load();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        save();
        return super.configure(req, json);
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        return true;
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return "企业微信通知";
    }

    public List<Descriptor<WechatNotifier.MessageType>> getMsgTypeDescriptors() {
        DescriptorExtensionList<WechatNotifier.MessageType, Descriptor<WechatNotifier.MessageType>> list = Jenkins.getInstance().getDescriptorList(WechatNotifier.MessageType.class);

        List<Descriptor<WechatNotifier.MessageType>> result = new ArrayList<>(list);
        Collections.sort(result, new Comparator<Descriptor<WechatNotifier.MessageType>>() {
            @Override
            public int compare(Descriptor<WechatNotifier.MessageType> o1, Descriptor<WechatNotifier.MessageType> o2) {
                return ((WechatNotifier.MessageTypeDescriptor) o1).getOrder() - ((WechatNotifier.MessageTypeDescriptor) o2).getOrder();
            }
        });
        return result;
    }

    public ListBoxModel doFillTypeItems() {
        ListBoxModel model = new ListBoxModel();
        model.add("个人", "user");
        model.add("群聊", "chat");
        model.add("部门", "party");
        model.add("标签", "tag");
        model.add("自定义分组", "group");
        return model;
    }

    public synchronized ListBoxModel doFillIdItems(@QueryParameter String type) {
        ListBoxModel model = new ListBoxModel();
        String title;
        switch (type) {
            case "":
            case "user":
                List<WechatUser> users = ContactsProvider.getInstance().getAllUsers();

                title = users.isEmpty() ? "无可选用户" : "请选择一个用户";
                model.add(title, "-1");
                for (WechatUser user : users) {
                    model.add(user.getName()+"("+user.getId()+")", user.getId());
                }
                return model;
            case "party":
                List<WechatDepartment> departments = ContactsProvider.getInstance().getDepartments();
                title = departments.isEmpty() ? "无可选部门" : "请选择一个部门";
                model.add(title, "-1");
                for (WechatDepartment department : departments) {
                    model.add(department.getDisplayName()+"("+department.getId()+")", department.getId());
                }
                return model;
            case "tag":
                List<WechatTag> tags = ContactsProvider.getInstance().getTags();
                title = tags.isEmpty() ? "无可选标签" : "请选择一个标签";
                model.add(title, "-1");
                for (WechatTag tag : tags) {
                    model.add(tag.getName(), tag.getId());
                }
                return model;
            case "group":
                List<CustomGroup> customGroups = ContactsProvider.getInstance().getCustomGroups();
                title = customGroups.isEmpty() ? "无自定义分组" : "请选择一个自定义分组";
                model.add(title,"-1");
                for (CustomGroup customGroup : customGroups) {
                    model.add(customGroup.getName());
                }
                return model;
            case "chat":
                List<Chat> chats = ContactsProvider.getInstance().getChats();
                title = chats.isEmpty() ? "无群聊" : "请选择一个群聊";
                model.add(title,"-1");
                for (Chat chat : chats) {
                    model.add(chat.getName()+"("+chat.getChatId()+")", chat.getChatId());
                }
                return model;
            default:
                return new ListBoxModel();
        }
    }
}
