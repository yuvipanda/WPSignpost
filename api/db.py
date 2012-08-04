import json
import time

from elixir import *

metadata.bind = 'sqlite:///posts.sqlite'
metadata.bind.echo = True

class Post(Entity):
    using_options(tablename='posts')

    permalink = Field(Unicode(255))
    title = Field(Unicode(255))
    content = Field(UnicodeText)
    author = Field(Unicode(255))
    author_link = Field(Unicode(255))
    image_link = Field(Unicode(255))
    issue = ManyToOne('Issue')

    def serialize(self):
        return {
            'id': self.id,
            'permalink': self.permalink,
            'title': self.title,
            'content': self.content,
            'author_name': self.author,
            'author_link': self.author_link,
            'image_url': self.image_link
        }

class Issue(Entity):
    using_options(tablename='issues')

    date = Field(DateTime)
    posts = OneToMany('Post')


    def serialize(self):
        return {
            'id': self.id,
            'date': time.mktime(self.date.timetuple())
        }
