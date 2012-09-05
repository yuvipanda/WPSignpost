import json
import time

from flask import Flask
from flask.ext.sqlalchemy import SQLAlchemy
import settings

db = SQLAlchemy()

def setup_script():
    app = Flask(__name__)
    app.config['SQLALCHEMY_DATABASE_URI'] = settings.DATABASE
    db.init_app(app)
    app.test_request_context().push()

def setup_app(app):
    app.config['SQLALCHEMY_DATABASE_URI'] = settings.DATABASE
    db.init_app(app)

class Post(db.Model):
    __tablename__ = "posts"

    id = db.Column(db.Integer, primary_key=True)
    permalink = db.Column(db.String(255), index=True)
    title = db.Column(db.String(1024))
    content = db.Column(db.Text())
    author = db.Column(db.String(255))
    author_link = db.Column(db.String(255))
    image_link = db.Column(db.String(1024))
    issue_id = db.Column(db.Integer, db.ForeignKey('issues.id'))
    issue = db.relationship('Issue', backref=db.backref('posts', lazy='dynamic'))

    def serialize(self, exclude_content=False):
        data = {
            'id': self.id,
            'permalink': self.permalink,
            'title': self.title,
            'author_name': self.author,
            'author_link': self.author_link,
            'image_url': self.image_link
        }
        if not exclude_content:
            data['content'] = self.content
        return data

class Issue(db.Model):
    __tablename__ = "issues"

    id = db.Column(db.Integer, primary_key=True)
    date = db.Column(db.DateTime, index=True)
    permalink = db.Column(db.String(1024), index=True)
    #posts = OneToMany('Post')

    def serialize(self):
        return {
            'id': self.id,
            'date': time.mktime(self.date.timetuple()),
            'permalink': self.permalink
        }

class Device(db.Model):
    __tablename__ = 'devices'

    id = db.Column(db.Integer, primary_key=True)
    regID = db.Column(db.String(1024))
    last_permalink = db.Column(db.String(1024), index=True)
