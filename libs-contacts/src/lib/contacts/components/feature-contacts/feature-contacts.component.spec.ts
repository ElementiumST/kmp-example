import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatureContactsComponent } from './feature-contacts.component';

describe('FeatureContactsComponent', () => {
  let component: FeatureContactsComponent;
  let fixture: ComponentFixture<FeatureContactsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatureContactsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatureContactsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
