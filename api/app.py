from db import *
from flask import *
import json
from werkzeug.contrib.cache import MemcachedCache
from werkzeug.contrib.atom import AtomFeed

from mwapi import MWApi
app = Flask(__name__)
app.debug = True

setup_app(app)

cache = MemcachedCache(['127.0.0.1:11211']) 

api = MWApi("http://en.wikipedia.org")

@app.route('/issues')
def first_issues():
    issues_data = cache.get("all_issues")
    if not issues_data:
        issues = Issue.query.order_by(Issue.date.desc()).all()
        data = [issue.serialize() for issue in issues]
        issues_data = json.dumps(data)
        cache.set("all_issues", issues_data)
    return (issues_data, 200, {'Content-Type': 'application/json'})

@app.route('/issues/<int:offset>')
def issues(offset):
    # Stub, here because I'm lazy
    # offset is ignored
    return first_issues()

@app.route('/issue/latest')
def latest_issue():
    issue_data = cache.get("latest_issue")
    if not issue_data:
        issue = Issue.query.order_by(Issue.date.desc()).limit(1).one()
        issue_data = json.dumps(issue.serialize())
        cache.set("latest_issue", issue_data)
    return (issue_data, 200, {'Content-Type': 'application/json'})

@app.route('/post/permalink/<path:permalink>')
def post_permalink(permalink):
    key = "post_" + permalink
    post_data = cache.get(key)
    if not post_data:
        post = Post.query.filter_by(permalink=permalink).one()
        post_data = json.dumps(post.serialize())
        cache.set(key, post_data)
    return (post_data, 200, {'Content-Type': 'application/json'})

@app.route('/issue/permalink/<path:permalink>')
def issue_permalink(permalink):
    key = "issue_" + permalink
    issue_data = cache.get(key)
    if not issue_data:
        issue = Issue.query.filter_by(permalink=permalink).one()
        posts = [post.serialize(True) for post in issue.posts]
        data = issue.serialize()
        data['posts'] = posts
        issue_data = json.dumps(data)
        cache.set(key, issue_data)
    return (issue_data, 200, {'Content-Type': 'application/json'})

@app.route('/issue/update/latest', methods=["POST"])
def update_latest_issue():
    from scraper import save_issue
    date = api.get({'action': 'expandtemplates', 'text': '{{Wikipedia:Wikipedia_Signpost/Issue|1}}'})['expandtemplates']['*']
    cache.set("latest_issue", None)
    cache.set("all_issues", None)
    cache.set("feed", None)
    save_issue(date)
    return push_latest_issue()

@app.route('/issue/push/latest', methods=["POST"])
def push_latest_issue():
    issue = Issue.query.order_by(Issue.date.desc()).limit(1).one()
    from push import push_issue
    return str(push_issue(issue.permalink))

@app.route('/device/register', methods=["POST"])
def register_device():
    regID = request.form['regID']
    if regID:
        if Device.query.filter_by(regID=regID).count() == 0:
            device = Device(regID=regID)
            db.session.add(device)
            db.session.commit() 
            return ("", 200)
        else:
            return ("regID already registered", 200)
    else:
        return ("No regID specified", 400) 

@app.route('/device/deregister', methods=["POST"])
def deregister_device():
    regID = request.form['regID']
    if regID:
        if Device.query.filter_by(regID=regID).count() != 0:
            device = Device.query.filter_by(regID=regID).one()
            db.session.delete(device)
            db.session.commit() 
        return ("", 200)
    else:
        return ("No regID specified", 400) 

@app.route('/feed')
def feed():
    issues = Issue.query.order_by(Issue.date.desc()).limit(3)
    resp_data = cache.get('feed')
    if resp_data:
        resp = make_response(resp_data)
    else:
        doc = AtomFeed('Signpost',
                feed_url=request.url, 
                url='https://en.wikipedia.org/wiki/Wikipedia:Wikipedia_Signpost'
                )
        for issue in issues:
            for post in issue.posts:
                doc.add(post.title, unicode(post.content),
                        content_type='html',
                        author=post.author,
                        url='https://' + post.permalink,
                        updated=issue.date,
                        xml_base='https://en.wikipedia.org'
                        )
        resp = doc.get_response()
        cache.set('feed', (resp.data, resp.status, resp.headers))
    return resp

if __name__ == '__main__':
    app.run()
