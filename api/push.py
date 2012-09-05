from db import *
from gcm import gcm

import settings

def data_for_issue(permalink):
    issue = Issue.query.filter_by(permalink=permalink).one()
    post_titles = [post.title for post in issue.posts] 
    return {
            'type': 'newIssue',
            'date': issue.date.strftime("%Y-%m-%d"),
            'permalink': issue.permalink,
            'text': ', '.join(post_titles)
    }


def push_issue(permalink):
    data = data_for_issue(permalink)
    g = gcm.GCM(settings.GCM_KEY)

    devices = Device.query.limit(750).offset(0)
    devices_length = devices.count()
    device_ids = [device.regID for device in devices if device.last_permalink != permalink]
    while device_ids:
        g.json_request(device_ids, data=data, collapse_key=permalink)
        print "Pushed for " + ','.join(device_ids)
        for device in devices:
            device.last_permalink = permalink
            db.session.add(device)
        db.session.commit()
        devices = Device.query.limit(750).offset(devices_length)
        devices_length = devices.count()
        device_ids = [device.regID for device in devices if device.last_permalink != permalink]
