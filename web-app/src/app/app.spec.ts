import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { KmpBridgeService } from 'data-access-kmp-bridge';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        {
          provide: KmpBridgeService,
          useValue: {
            ready: signal(false),
            rootChildKind: signal('AUTH'),
            contactsChildKind: signal('LIST'),
            initialize: () => Promise.resolve(),
          },
        },
      ],
    }).compileComponents();
  });

  it('should create app', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    expect(fixture.componentInstance).toBeTruthy();
  });
});
